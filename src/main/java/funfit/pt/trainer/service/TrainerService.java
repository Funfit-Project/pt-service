package funfit.pt.trainer.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.trainer.dto.ReadMemberResponse;
import funfit.pt.user.entity.User;
import funfit.pt.user.repository.UserRepository;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final JwtUtils jwtUtils;

    public Slice<ReadMemberResponse> readAllMembers(HttpServletRequest request, Pageable pageable) {
        String email = jwtUtils.getEmailFromHeader(request);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        Slice<Relationship> membersSlice = relationshipRepository.findSliceByTrainer(user, pageable);
        return membersSlice.map(relationship -> new ReadMemberResponse(user.getName(), relationship.getMember().getName(),
                relationship.getCenterName(), relationship.getRegistrationCount(), relationship.getRegistrationCount()));
    }
}
