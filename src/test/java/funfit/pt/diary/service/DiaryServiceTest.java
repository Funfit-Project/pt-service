package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatAndUpdateCommentRequest;
import funfit.pt.diary.dto.CreateAndUpdatePostRequest;
import funfit.pt.diary.entity.Category;
import funfit.pt.diary.entity.Comment;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DiaryServiceTest {

    @Autowired private DiaryService diaryService;
    @Autowired private PostRepository postRepository;
    @Autowired private RelationshipRepository relationshipRepository;

    @Test
    @DisplayName("수업일지 등록 성공")
    public void createPtLogTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        // when
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("url1");
        imageUrls.add("url2");
        CreateAndUpdatePostRequest createAndUpdatePostRequest = new CreateAndUpdatePostRequest("오늘 운동일지입니다.", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "수업일지", createAndUpdatePostRequest, "trainer@naver.com");

        // then
        Post savedPost = postRepository.findById(postId).get();
        assertThat(savedPost.getWriterEmail()).isEqualTo("trainer@naver.com");
        assertThat(savedPost.getContent()).isEqualTo("오늘 운동일지입니다.");
        assertThat(savedPost.getCategory()).isEqualTo(Category.PT_LOG);
        assertThat(savedPost.getImages().size()).isEqualTo(2);
        assertThat(savedPost.getComments().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("수업일지 등록 실패-트레이너가 아닌 사람이 작성")
    public void createPtLogTestFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        // when
        CreateAndUpdatePostRequest createAndUpdatePostRequest = new CreateAndUpdatePostRequest("오늘 운동일지입니다.", null);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.createPost(relationship.getId(), "수업일지", createAndUpdatePostRequest, "member@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
    }

    @Test
    @DisplayName("다이어리 등록 성공")
    public void createDairyTestSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        // when
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("url1");
        imageUrls.add("url2");
        CreateAndUpdatePostRequest createAndUpdatePostRequest = new CreateAndUpdatePostRequest("오늘의 식단!", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "다이어리", createAndUpdatePostRequest, "member@naver.com");

        // then
        Post savedPost = postRepository.findById(postId).get();
        assertThat(savedPost.getWriterEmail()).isEqualTo("member@naver.com");
        assertThat(savedPost.getContent()).isEqualTo("오늘의 식단!");
        assertThat(savedPost.getCategory()).isEqualTo(Category.DIARY);
        assertThat(savedPost.getImages().size()).isEqualTo(2);
        assertThat(savedPost.getComments().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("다이어리 등록 실패-회원이 아닌 사람이 작성")
    public void createDiaryFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);

        // when
        CreateAndUpdatePostRequest createAndUpdatePostRequest = new CreateAndUpdatePostRequest("오늘의 식단!", null);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.createPost(relationship.getId(), "다이어리", createAndUpdatePostRequest, "trainer@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_CREATE_DIARY);
    }

    @Test
    @DisplayName("게시글 수정 성공")
    public void updatePostSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("url1");
        imageUrls.add("url2");
        CreateAndUpdatePostRequest requestDto = new CreateAndUpdatePostRequest("오늘의 식단!", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "다이어리", requestDto, "member@naver.com");

        // when
        List<String> newImageUrls = new ArrayList<>();
        newImageUrls.add("url3");
        newImageUrls.add("url4");
        CreateAndUpdatePostRequest newRequestDto = new CreateAndUpdatePostRequest("오늘의 운동!", newImageUrls);
        diaryService.updatePost(postId, newRequestDto, "member@naver.com");

        // then
        Post post = postRepository.findById(postId).get();
        Assertions.assertThat(post.getContent()).isEqualTo("오늘의 운동!");
        Assertions.assertThat(post.getImages().size()).isEqualTo(2);
        Assertions.assertThat(post.getImages().get(0).getUrl()).isEqualTo("url3");
        Assertions.assertThat(post.getImages().get(1).getUrl()).isEqualTo("url4");
    }

    @Test
    @DisplayName("게시글 수정 실패-권한 없는 사용자")
    public void updatePostFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // when
        CreateAndUpdatePostRequest newRequestDto = new CreateAndUpdatePostRequest("오늘의 운동!", new ArrayList<>());

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.updatePost(post.getId(), newRequestDto, "otherMember@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    public void deletePostSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // when
        diaryService.deletePost(post.getId(), "member@naver.com");

        // then
        Assertions.assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("게시글 삭제 실패-권한 없는 사용자")
    public void deletePostFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.deletePost(post.getId(), "otherMember@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    public void addCommentSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        List<String> imageUrls = new ArrayList<>();
        CreateAndUpdatePostRequest requestDto = new CreateAndUpdatePostRequest("오늘의 식단!", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "다이어리", requestDto, "member@naver.com");

        // when
        diaryService.addComment(relationship.getId(), postId, new CreatAndUpdateCommentRequest("댓글 등록"), "member@naver.com");

        // then
        Assertions.assertThat(postRepository.findById(postId).get().getComments().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    public void updateCommentSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // when
        diaryService.updateComment(comment.getId(), new CreatAndUpdateCommentRequest("굿굿!"), "trainer@naver.com");

        // then
        Assertions.assertThat(postRepository.findById(post.getId()).get().getComments().size()).isEqualTo(1);
        Assertions.assertThat(postRepository.findById(post.getId()).get().getComments().get(0).getContent()).isEqualTo("굿굿!");
    }

    @Test
    @DisplayName("댓글 수정 실패-권한 없는 사용자")
    public void updateCommentFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.updateComment(comment.getId(), new CreatAndUpdateCommentRequest("굿굿!"), "otherMember@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    public void deleteCommentSuccess() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // when
        diaryService.deleteComment(comment.getId(), "trainer@naver.com");

        // then
        Assertions.assertThat(postRepository.findById(post.getId()).get().getComments().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("댓글 삭제 실패-권한 없는 사용자")
    public void deleteCommentFailByAuthority() {
        // given
        Relationship relationship = Relationship.create("member@naver.com", "trainer@naver.com", "펀핏짐", 10);
        relationshipRepository.save(relationship);
        Post post = Post.create(relationship, "member@naver.com", "오늘의 식단입니다.", Category.DIARY);
        Comment comment = Comment.create("trainer@naver.com", "굿!");
        post.addComment(comment);
        postRepository.save(post);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () ->  diaryService.deleteComment(comment.getId(), "otherMember@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
