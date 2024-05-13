package funfit.pt.schedule.controller;

import funfit.pt.dto.SuccessResponse;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.dto.ReadScheduleResponse;
import funfit.pt.schedule.service.ScheduleService;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtUtils jwtUtils;

    @GetMapping("/pt/schedule")
    public ResponseEntity readSchedule(HttpServletRequest request) {
        String userEmail = jwtUtils.getEmailFromHeader(request);
        ReadScheduleResponse readScheduleResponse = scheduleService.readSchedule(userEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("스케줄 조회 성공", readScheduleResponse));
    }

    @PostMapping("/pt/schedule")
    public ResponseEntity addSchedule(@RequestBody AddScheduleRequest addScheduleRequest, HttpServletRequest request) {
        String userEmail = jwtUtils.getEmailFromHeader(request);
        AddScheduleResponse addScheduleResponse = scheduleService.addSchedule(addScheduleRequest, userEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수업 예약 성공", addScheduleResponse));
    }
}
