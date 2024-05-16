package funfit.pt.diary.controller;

import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.dto.CreateDiaryResponse;
import funfit.pt.diary.service.DiaryService;
import funfit.pt.dto.SuccessResponse;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final JwtUtils jwtUtils;

    @PostMapping("/pt/{relationshipId}/pt_log")
    public ResponseEntity createPtLog(@PathVariable long relationshipId,
                                      @Validated @RequestBody CreatePostRequest createPostRequest,
                                      HttpServletRequest request) {
        CreateDiaryResponse createDiaryResponse = diaryService.createPtLog(relationshipId, createPostRequest, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수업일지 등록 성공", createDiaryResponse));
    }

    @PostMapping("/pt/{relationshipId}/diary")
    public ResponseEntity createDiary(@PathVariable long relationshipId,
                                      @Validated @RequestBody CreatePostRequest createPostRequest,
                                      HttpServletRequest request) {
        CreateDiaryResponse createDiaryResponse = diaryService.createDiary(relationshipId, createPostRequest, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("다이어리 등록 성공", createDiaryResponse));
    }
}
