package funfit.pt.schedule.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import funfit.pt.user.entity.User;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RelationshipRepository relationshipRepository;
    private final JwtUtils jwtUtils;

    public AddScheduleResponse addSchedule(AddScheduleRequest addScheduleRequest, long relationshipId, HttpServletRequest request) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        validateAuthority(relationship, request);
        validateDuplicate(addScheduleRequest.getDate(), relationship.getTrainer());

        Schedule schedule = Schedule.create(relationship, addScheduleRequest.getDate(), addScheduleRequest.getMemo());
        scheduleRepository.save(schedule);
        return new AddScheduleResponse(schedule.getDate(), schedule.getMemo());
    }

    private void validateAuthority(Relationship relationship, HttpServletRequest request) {
        String email = jwtUtils.getEmailFromHeader(request);
        if (!relationship.getMember().getEmail().equals(email) && !relationship.getTrainer().getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateDuplicate(LocalDateTime date, User trainer) {
        List<Schedule> schedules = scheduleRepository.findByTrainer(trainer);
        boolean isAlreadyExist = schedules.stream()
                .anyMatch(schedule -> schedule.getDate().equals(date));
        if (isAlreadyExist) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVATION);
        }
    }
}
