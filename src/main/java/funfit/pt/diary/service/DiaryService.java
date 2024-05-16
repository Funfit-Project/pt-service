package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.dto.CreateDiaryResponse;
import funfit.pt.diary.entity.Category;
import funfit.pt.diary.entity.Image;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;

    public CreateDiaryResponse createPtLog(long relationshipId, CreatePostRequest createPostRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateCreatePtLogAuthority(relationship, email);

        Post post = Post.create(email, createPostRequest.getContent(), Category.PT_LOG);
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            createPostRequest.getImageUrls()
                    .stream()
                    .map(url -> Image.create(url, post))
                    .toList();
        }

        postRepository.save(post);

        return new CreateDiaryResponse(email, post.getContent(), post.getCategory().getName(), createPostRequest.getImageUrls());
    }

    private void validateCreatePtLogAuthority(Relationship relationship, String email) {
        if (!relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
        }
    }

    public CreateDiaryResponse createDiary(long relationshipId, CreatePostRequest createPostRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateCreateDiaryAuthority(relationship, email);

        Post post = Post.create(email, createPostRequest.getContent(), Category.DIARY);
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            createPostRequest.getImageUrls()
                    .stream()
                    .map(url -> Image.create(url, post))
                    .toList();
        }

        postRepository.save(post);

        return new CreateDiaryResponse(email, post.getContent(), post.getCategory().getName(), createPostRequest.getImageUrls());
    }

    private void validateCreateDiaryAuthority(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
        }
    }
}
