package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.entity.Category;
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
        CreatePostRequest createPostRequest = new CreatePostRequest("오늘 운동일지입니다.", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "수업일지", createPostRequest, "trainer@naver.com");

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
        CreatePostRequest createPostRequest = new CreatePostRequest("오늘 운동일지입니다.", null);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.createPost(relationship.getId(), "수업일지", createPostRequest, "member@naver.com"))
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
        CreatePostRequest createPostRequest = new CreatePostRequest("오늘의 식단!", imageUrls);
        long postId = diaryService.createPost(relationship.getId(), "다이어리", createPostRequest, "member@naver.com");

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
        CreatePostRequest createPostRequest = new CreatePostRequest("오늘의 식단!", null);

        // then
        Assertions.assertThat(assertThrows(BusinessException.class, () -> diaryService.createPost(relationship.getId(), "다이어리", createPostRequest, "trainer@naver.com"))
                .getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_CREATE_DIARY);
    }
}
