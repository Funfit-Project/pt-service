package funfit.pt.schedule.controller;

import funfit.pt.dto.SuccessResponse;
import funfit.pt.query.ScheduleQueryService;
import funfit.pt.schedule.dto.AddAndDeleteScheduleRequest;
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
    private final ScheduleQueryService scheduleQueryService;
    private final JwtUtils jwtUtils;

    @GetMapping("/pt/schedule")
    public ResponseEntity readSchedule(HttpServletRequest request) {
        String userEmail = jwtUtils.getEmailFromHeader(request);
        ReadScheduleResponse readScheduleResponse = scheduleQueryService.readSchedule(userEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("스케줄 조회 성공", readScheduleResponse));
    }

    @PostMapping("/pt/schedule")
    public ResponseEntity addSchedule(@RequestBody AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, HttpServletRequest request) {
        String userEmail = jwtUtils.getEmailFromHeader(request);
        AddScheduleResponse addScheduleResponse = scheduleService.addSchedule(addAndDeleteScheduleRequest, userEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수업 예약 성공", addScheduleResponse));
    }

    @DeleteMapping("/pt/schedule")
    public ResponseEntity deleteSchedule(@RequestBody AddAndDeleteScheduleRequest addAndDeleteScheduleRequest, HttpServletRequest request) {
        String userEmail = jwtUtils.getEmailFromHeader(request);
        scheduleService.deleteSchedule(addAndDeleteScheduleRequest, userEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수업 취소 성공", null));
    }
}
