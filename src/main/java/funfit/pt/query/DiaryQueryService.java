package funfit.pt.query;

import funfit.pt.api.dto.User;
import funfit.pt.diary.dto.ReadPostInListResponse;
import funfit.pt.diary.dto.ReadPostResponse;
import funfit.pt.diary.entity.Post;
import funfit.pt.diary.repository.PostRepository;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.api.UserDataProvider;
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

    private final UserDataProvider userDataProvider;
    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;

    public ReadPostResponse readPost(long relationshipId, long postId, String email) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RELATIONSHIP_ID));
        validateReadAuthority(relationship, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));

        User postUser = userDataProvider.getUser(post.getWriterEmail());
        List<ReadPostResponse.CommentDto> commentDtos = post.getComments()
                .stream()
                .map(comment -> {
                    User commentUser = userDataProvider.getUser(comment.getWriterEmail());
                    if (commentUser == null) {
                        return new ReadPostResponse.CommentDto("알수없음", comment.getContent());
                    } else {
                        return new ReadPostResponse.CommentDto(commentUser.getUserName(), comment.getContent());
                    }
                })
                .toList();
        List<String> imageUrls = post.getImages()
                .stream()
                .map(image -> image.getUrl())
                .toList();
        if (postUser == null) {
            return new ReadPostResponse("알수없음", post.getContent(), post.getCategory().getName(), imageUrls, commentDtos);
        } else {
            return new ReadPostResponse(postUser.getUserName(), post.getContent(), post.getCategory().getName(), imageUrls, commentDtos);
        }
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
