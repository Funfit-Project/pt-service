package funfit.pt.query;

import funfit.pt.diary.dto.ReadPostInListResponse;
import funfit.pt.diary.dto.ReadPostResponse;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.rabbitMq.entity.User;
import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryQueryService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;

    public ReadPostResponse readPost(long relationshipId, long postId, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateReadAuthority(relationship, email);

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

    public Slice<ReadPostInListResponse> readPostList(long relationshipId, Pageable pageable, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateReadAuthority(relationship, email);

        return postRepository.findByRelationship(relationship, pageable)
                .map(post -> new ReadPostInListResponse(post.getId(), post.getImages().get(0).getUrl()));
    }

    private void validateReadAuthority(Relationship relationship, String email) {
        if (!relationship.getMemberEmail().equals(email) && !relationship.getTrainerEmail().equals(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
