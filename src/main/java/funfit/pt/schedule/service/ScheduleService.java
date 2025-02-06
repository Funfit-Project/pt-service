package funfit.pt.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import funfit.pt.outbox.Outbox;
import funfit.pt.outbox.OutboxRepository;
import funfit.pt.api.AuthServiceClient;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.exception.customException.ExternalServiceException;
import funfit.pt.kafka.dto.CompensatePointsDto;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.*;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RelationshipRepository relationshipRepository;
    private final RedissonClient redissonClient;
    private final AuthServiceClient authServiceClient;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 동시성 제어: Redis 분산 락
     */
    public AddScheduleResponse addSchedule(AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, String memberEmail) throws JsonProcessingException {
        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        RLock lock = redissonClient.getLock(relationship.getTrainerEmail());

        try {
            boolean isLocked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Redis 락 획득 실패");
            } else {
                validateDuplicate(relationship.getTrainerEmail(), addAndDeleteScheduleRequest.getDateTime());
                UUID uuid = UUID.randomUUID();

                try {
                    boolean result = authServiceClient.deductPoints(new CompensatePointsDto(memberEmail, uuid.toString()));
                    if (!result) { // 포인트 부족 -> 예외 응답 반환
                        throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
                    } else {
                        Schedule schedule = Schedule.create(relationship, addAndDeleteScheduleRequest.getDateTime());
                        scheduleRepository.save(schedule);
                        return new AddScheduleResponse(schedule.getDateTime());
                    }
                } catch (BusinessException e) {
                    throw e;
                } catch (ExternalServiceException e) { // auth 서비스 장애 -> 보상 트랜잭션(포인트 차감되었다면 복구) + 예외 응답 반환
                    Outbox outbox = Outbox.create(objectMapper.writeValueAsString(new CompensatePointsDto(memberEmail, uuid.toString())).toString(), "compensate-points");
                    outboxRepository.save(outbox);
                    throw e;
                } catch (RuntimeException e) { // 예약 실패(ex, 롤백) -> 보상 트랜잭션(포인트 차감되었다면 복구) + 예외 응답 반환
                    Outbox outbox = Outbox.create(objectMapper.writeValueAsString(new CompensatePointsDto(memberEmail, uuid.toString())).toString(), "compensate-points");
                    outboxRepository.save(outbox);
                    throw new BusinessException(ErrorCode.RESERVATION_FAIL);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 동시성 제어: 유니크 제약 조건
     */
//    public AddScheduleResponse addSchedule(AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, String memberEmail) {
//        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
//                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
//
//        try {
//            validateDuplicate(relationship.getTrainerEmail(), addAndDeleteScheduleRequest.getDateTime());
//            Schedule schedule = Schedule.create(relationship, addAndDeleteScheduleRequest.getDateTime());
//            scheduleRepository.save(schedule);
//            return new AddScheduleResponse(schedule.getDateTime());
//        } catch (DataIntegrityViolationException e) {
//            throw new BusinessException(ErrorCode.ALREADY_RESERVATION);
//        }
//    }


    private void validateDuplicate(String trainerEmail, LocalDateTime dateTime) {
        if (scheduleRepository.findByTrainerEmailAndDateTime(trainerEmail, dateTime).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVATION);
        }
    }

    @Transactional
    public void deleteSchedule(AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, String memberEmail) {
        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Schedule schedule = scheduleRepository.findByRelationshipAndDateTime(relationship, addAndDeleteScheduleRequest.getDateTime())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        scheduleRepository.delete(schedule);
    }
}
