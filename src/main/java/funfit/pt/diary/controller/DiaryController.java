package funfit.pt.diary.controller;

import funfit.pt.diary.dto.CreatCommentRequest;
import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.dto.ReadPostResponse;
import funfit.pt.diary.service.DiaryService;
import funfit.pt.dto.SuccessResponse;
import funfit.pt.query.DiaryQueryService;
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
    private final DiaryQueryService diaryQueryService;
    private final JwtUtils jwtUtils;

    @PostMapping("/pt/{relationshipId}")
    public ResponseEntity createPost(@PathVariable long relationshipId,
                                     @RequestParam("category") String category,
                                     @Validated @RequestBody CreatePostRequest createPostRequest,
                                     HttpServletRequest request) {
        long postId = diaryService.createPost(relationshipId, category, createPostRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("등록 성공", readPostResponse));
    }

    @PostMapping("/pt/{relationshipId}/{postId}/comment")
    public ResponseEntity addComment(@PathVariable long relationshipId, @PathVariable long postId,
                                     @Validated @RequestBody CreatCommentRequest creatCommentRequest, HttpServletRequest request) {
        diaryService.addComment(relationshipId, postId, creatCommentRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("댓글 등록 성공", readPostResponse));
    }

    @GetMapping("/pt/{relationshipId}/{postId}")
    public ResponseEntity readPost(@PathVariable long relationshipId, @PathVariable long postId, HttpServletRequest request) {
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 조회 성공", readPostResponse));
    }
}
