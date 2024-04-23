package funfit.pt.schedule.controller;

import funfit.pt.responseDto.SuccessResponse;
import funfit.pt.schedule.dto.AddScheduleRequest;
import funfit.pt.schedule.dto.AddScheduleResponse;
import funfit.pt.schedule.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/api/{relationshipId}/add")
    public ResponseEntity addSchedule(@RequestBody AddScheduleRequest addScheduleRequest,
                                      @PathVariable("relationshipId") long relationshipId,
                                      HttpServletRequest request) {
        AddScheduleResponse addScheduleResponse = scheduleService.addSchedule(addScheduleRequest, relationshipId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수업 예약 성공", addScheduleResponse));
    }
}
