package funfit.pt.diary.service;

import funfit.pt.diary.dto.CreatCommentRequest;
import funfit.pt.diary.dto.CreatePostRequest;
import funfit.pt.diary.dto.ReadPostResponse;
import funfit.pt.diary.entity.Category;
import funfit.pt.diary.entity.Comment;
import funfit.pt.diary.entity.Image;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.UserService;
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
    private final UserService userService;

    public ReadPostResponse createPost(long relationshipId, String categoryName, CreatePostRequest createPostRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));

        Category category = Category.find(categoryName);
        if (category == Category.PT_LOG) {
            validateCreatePtLogAuthority(relationship, email);
        }
        if (category == Category.DIARY) {
            validateCreateDiaryAuthority(relationship, email);
        }

        Post post = Post.create(email, createPostRequest.getContent(), category);
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            createPostRequest.getImageUrls()
                    .stream()
                    .forEach(url -> {
                        post.addImage(Image.create(url));
                    });
        }

        postRepository.save(post);

        User postUser = userService.getUser(email);
        List<ReadPostResponse.CommentDto> commentDtos = post.getComments()
                .stream()
                .map(comment -> {
                    User commentUser = userService.getUser(comment.getWriterEmail());
                    return new ReadPostResponse.CommentDto(commentUser.getUserName(), comment.getContent());
                })
                .toList();
        return new ReadPostResponse(postUser.getUserName(), post.getContent(), post.getCategory().getName(), createPostRequest.getImageUrls(), commentDtos);
    }

    private void validateCreatePtLogAuthority(Relationship relationship, String email) {
        if (!relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
        }
    }

    private void validateCreateDiaryAuthority(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_CREATE_PT_LOG);
        }
    }

    public ReadPostResponse addComment(long relationshipId, long postId, CreatCommentRequest creatCommentRequest, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateRelationshipAuthority(relationship, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        post.addComment(Comment.create(email, creatCommentRequest.getContent()));

        User postUser = userService.getUser(post.getWriterEmail());
        List<ReadPostResponse.CommentDto> commentDtos = post.getComments()
                .stream()
                .map(comment -> {
                    User commentUser = userService.getUser(comment.getWriterEmail());
                    return new ReadPostResponse.CommentDto(commentUser.getUserName(), comment.getContent());
                })
                .toList();
        List<String> imageUrls = post.getImages()
                .stream()
                .map(image -> image.getUrl())
                .toList();
        return new ReadPostResponse(postUser.getUserName(), post.getContent(), post.getCategory().getName(), imageUrls, commentDtos);
    }

    public ReadPostResponse readPost(long relationshipId, long postId, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateRelationshipAuthority(relationship, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        User postUser = userService.getUser(post.getWriterEmail());
        List<ReadPostResponse.CommentDto> commentDtos = post.getComments()
                .stream()
                .map(comment -> {
                    User commentUser = userService.getUser(comment.getWriterEmail());
                    return new ReadPostResponse.CommentDto(commentUser.getUserName(), comment.getContent());
                })
                .toList();
        List<String> imageUrls = post.getImages()
                .stream()
                .map(image -> image.getUrl())
                .toList();
        return new ReadPostResponse(postUser.getUserName(), post.getContent(), post.getCategory().getName(), imageUrls, commentDtos);
    }

    private void validateRelationshipAuthority(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email) && !relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
