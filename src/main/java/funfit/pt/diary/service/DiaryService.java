package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatAndUpdateCommentRequest;
import funfit.pt.diary.dto.CreateAndUpdatePostRequest;
import funfit.pt.diary.entity.Category;
import funfit.pt.diary.entity.Comment;
import funfit.pt.diary.entity.Image;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.CommentRepository;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryService {

    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;
    private final CommentRepository commentRepository;

    public long createPost(long relationshipId, String categoryName, CreateAndUpdatePostRequest createAndUpdatePostRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));

        Category category = Category.find(categoryName);
        validateUserInRelationship(relationship, email);
        validateCreatePostAuthority(category, relationship, email);

        Post post = Post.create(email, createAndUpdatePostRequest.getContent(), category);
        if (createAndUpdatePostRequest.getImageUrls() != null && !createAndUpdatePostRequest.getImageUrls().isEmpty()) {
            createAndUpdatePostRequest.getImageUrls()
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

    public long updatePost(long postId, CreateAndUpdatePostRequest createAndUpdatePostRequest, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        validateUpdateAndDeletePost(post, email);

        post.updateContent(createAndUpdatePostRequest.getContent());

        if (createAndUpdatePostRequest.getImageUrls() != null && !createAndUpdatePostRequest.getImageUrls().isEmpty()) {
            List<Image> images = createAndUpdatePostRequest.getImageUrls().stream()
                    .map(imageUrl -> Image.create(imageUrl))
                    .toList();
            post.updateImages(images);
        }

        return post.getId();
    }

    public void deletePost(long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        validateUpdateAndDeletePost(post, email);
        postRepository.delete(post);
    }

    private void validateUpdateAndDeletePost(Post post, String email) {
        if (!post.getWriterEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void addComment(long relationshipId, long postId, CreatAndUpdateCommentRequest creatAndUpdateCommentRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateUserInRelationship(relationship, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        post.addComment(Comment.create(email, creatAndUpdateCommentRequest.getContent()));
    }

    private void validateUserInRelationship(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email) && !relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    public long updateComment(long commentId, CreatAndUpdateCommentRequest creatAndUpdateCommentRequest, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
        validateUpdateAndDeleteComment(comment, email);
        comment.updateContent(creatAndUpdateCommentRequest.getContent());
        return comment.getId();
    }

    public void deleteComment(long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));
        validateUpdateAndDeleteComment(comment, email);
        commentRepository.delete(comment);
    }

    private void validateUpdateAndDeleteComment(Comment comment, String email) {
        if (!comment.getWriterEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
