package funfit.pt.schedule.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.*;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RelationshipRepository relationshipRepository;

    public AddScheduleResponse addSchedule(AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, String memberEmail) {
        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        try {
            validateDuplicate(relationship.getTrainerEmail(), addAndDeleteScheduleRequest.getDateTime());
            Schedule schedule = Schedule.create(relationship, addAndDeleteScheduleRequest.getDateTime());
            scheduleRepository.save(schedule);
            return new AddScheduleResponse(schedule.getDateTime());
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVATION);
        }
    }

    private void validateDuplicate(String trainerEmail, LocalDateTime dateTime) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findByTrainerEmailAndDateTime(trainerEmail, dateTime);
        if (optionalSchedule.isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVATION);
        }
    }

    public void deleteSchedule(AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, String memberEmail) {
        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Schedule schedule = scheduleRepository.findByRelationshipAndDateTime(relationship, addAndDeleteScheduleRequest.getDateTime())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        scheduleRepository.delete(schedule);
    }
}
