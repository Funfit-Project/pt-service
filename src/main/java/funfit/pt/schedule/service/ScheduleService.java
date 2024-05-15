package funfit.pt.schedule.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RelationshipRepository relationshipRepository;
    private final UserService userService;

    public ReadScheduleResponse readSchedule(String userEmail) {
        User user = userService.getUser(userEmail);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(23).withMinute(59).withSecond(59).withNano(59);

        List<Schedule> schedules = scheduleRepository.findByWeek(startOfWeek, endOfWeek);

        List<ReadScheduleResponse.ScheduleDto> scheduleDtos = schedules
                .stream()
                .map(schedule -> {
                    User member = userService.getUser(schedule.getRelationship().getMemberEmail());
                    return new ReadScheduleResponse.ScheduleDto(schedule.getDateTime(), member.getUserName());
                })
                .toList();

        return new ReadScheduleResponse(user.getRole().getName(), scheduleDtos);
    }

    public AddScheduleResponse addSchedule(AddScheduleRequest addScheduleRequest, String memberEmail) {
        Relationship relationship = relationshipRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        try {
            validateDuplicate(relationship.getTrainerEmail(), addScheduleRequest.getDateTime());
            Schedule schedule = Schedule.create(relationship, addScheduleRequest.getDateTime());
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
}
