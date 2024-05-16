package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatCommentRequest;
import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.entity.Category;
import funfit.pt.diary.entity.Comment;
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

    public long createPost(long relationshipId, String categoryName, CreatePostRequest createPostRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));

        Category category = Category.find(categoryName);
        validateUserInRelationship(relationship, email);
        validateCreatePostAuthority(category, relationship, email);

        Post post = Post.create(email, createPostRequest.getContent(), category);
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            createPostRequest.getImageUrls()
                    .stream()
                    .forEach(url -> {
                        post.addImage(Image.create(url));
                    });
        }
        postRepository.save(post);
        return post.getId();
    }

    private void validateCreatePostAuthority(Category category, Relationship relationship, String email) {
        if (category == Category.PT_LOG && !email.equals(relationship.getTrainerEmail())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
        }

        if (category == Category.DIARY && !email.equals(relationship.getMemberEmail())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_DIARY);
        }
    }

    public void addComment(long relationshipId, long postId, CreatCommentRequest creatCommentRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateUserInRelationship(relationship, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        post.addComment(Comment.create(email, creatCommentRequest.getContent()));
    }

    private void validateUserInRelationship(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email) && !relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
