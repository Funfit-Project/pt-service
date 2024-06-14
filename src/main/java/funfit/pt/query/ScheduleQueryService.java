package funfit.pt.query;

import funfit.pt.api.dto.User;
import funfit.pt.api.UserService;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.entity.Schedule;
import funfit.pt.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleQueryService {

    private final ScheduleRepository scheduleRepository;
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
                    User reservedMember = userService.getUser(schedule.getRelationship().getMemberEmail());
                    return new ReadScheduleResponse.ScheduleDto(schedule.getDateTime(), reservedMember.getUserName(), reservedMember.equals(user));
                })
                .toList();
        return new ReadScheduleResponse(scheduleDtos.size(), scheduleDtos);
    }
}
