package funfit.pt.diary.controller;

import funfit.pt.diary.dto.CreatAndUpdateCommentRequest;
import funfit.pt.diary.dto.CreateAndUpdatePostRequest;
import funfit.pt.diary.dto.ReadPostInListResponse;
import funfit.pt.diary.dto.ReadPostResponse;
import funfit.pt.diary.service.DiaryService;
import funfit.pt.dto.SuccessResponse;
import funfit.pt.query.DiaryQueryService;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryQueryService diaryQueryService;
    private final JwtUtils jwtUtils;

    @PostMapping("/pt/{relationshipId}")
    public ResponseEntity createPost(@PathVariable long relationshipId,
                                     @RequestParam("category") String category,
                                     @Validated @RequestBody CreateAndUpdatePostRequest createAndUpdatePostRequest,
                                     HttpServletRequest request) {
        long postId = diaryService.createPost(relationshipId, category, createAndUpdatePostRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("등록 성공", readPostResponse));
    }

    @PutMapping("/pt/{relationshipId}/{postId}")
    public ResponseEntity updatePost(@PathVariable long relationshipId, @PathVariable long postId,
                                     @Validated @RequestBody CreateAndUpdatePostRequest createAndUpdatePostRequest,
                                     HttpServletRequest request) {
        long savedPostId = diaryService.updatePost(postId, createAndUpdatePostRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, savedPostId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("수정 성공", readPostResponse));
    }

    @DeleteMapping("/pt/{relationshipId}/{postId}")
    public ResponseEntity deletePost(@PathVariable long relationshipId, @PathVariable long postId,
                                     HttpServletRequest request) {
        diaryService.deletePost(postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 삭제 성공", null));
    }

    @PostMapping("/pt/{relationshipId}/{postId}/comment")
    public ResponseEntity addComment(@PathVariable long relationshipId, @PathVariable long postId,
                                     @Validated @RequestBody CreatAndUpdateCommentRequest creatAndUpdateCommentRequest, HttpServletRequest request) {
        diaryService.addComment(relationshipId, postId, creatAndUpdateCommentRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("댓글 등록 성공", readPostResponse));
    }

    @PutMapping("/pt/{relationshipId}/{postId}/comment/{commentId}")
    public ResponseEntity updateComment(@PathVariable long relationshipId, @PathVariable long postId, @PathVariable long commentId,
                                        @Validated @RequestBody CreatAndUpdateCommentRequest creatAndUpdateCommentRequest, HttpServletRequest request) {
        diaryService.updateComment(commentId, creatAndUpdateCommentRequest, jwtUtils.getEmailFromHeader(request));
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("댓글 수정 성공", readPostResponse));
    }

    @DeleteMapping("/pt/{relationshipId}/{postId}/comment/{commentId}")
    public ResponseEntity deletePost(@PathVariable long relationshipId, @PathVariable long postId, @PathVariable long commentId,
                                     HttpServletRequest request) {
        diaryService.deleteComment(commentId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("댓글 삭제 성공", null));
    }

    @GetMapping("/pt/{relationshipId}/{postId}")
    public ResponseEntity readPost(@PathVariable long relationshipId, @PathVariable long postId, HttpServletRequest request) {
        ReadPostResponse readPostResponse = diaryQueryService.readPost(relationshipId, postId, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 단일 조회 성공", readPostResponse));
    }

    @GetMapping("/pt/{relationshipId}/posts")
    public ResponseEntity readPostList(@PathVariable long relationshipId,
                                       @PageableDefault(size = 9, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                       HttpServletRequest request) {
        Slice<ReadPostInListResponse> postList = diaryQueryService.readPostList(relationshipId, pageable, jwtUtils.getEmailFromHeader(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("게시글 리스트 조회 성공", postList));
    }
}
