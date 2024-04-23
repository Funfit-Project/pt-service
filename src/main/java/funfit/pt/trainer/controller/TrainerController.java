package funfit.pt.trainer.controller;

import funfit.pt.responseDto.SuccessResponse;
import funfit.pt.trainer.dto.ReadMemberResponse;
import funfit.pt.trainer.service.TrainerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping("/api/trainer/members")
    public ResponseEntity readAllMembers(@PageableDefault(size = 5, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                         HttpServletRequest request) {
        Slice<ReadMemberResponse> readMemberResponses = trainerService.readAllMembers(request, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("회원 리스트 조회 성공", readMemberResponses));
    }
}
