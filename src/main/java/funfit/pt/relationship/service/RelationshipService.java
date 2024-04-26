package funfit.pt.relationship.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.dto.AddTrainerRequest;
import funfit.pt.relationship.dto.AddTrainerResponse;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import funfit.pt.schedule.repository.ScheduleRepository;
import funfit.pt.user.entity.User;
import funfit.pt.user.repository.UserRepository;
import funfit.pt.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public AddTrainerResponse addTrainer(AddTrainerRequest addTrainerRequest, HttpServletRequest request) {
        String email = jwtUtils.getEmailFromHeader(request);
        User member = validateRole(email);
        User trainer = validateUserCode(addTrainerRequest.getUserCode());
        validateDuplicate(member, trainer);

        Relationship relationship = Relationship.create(trainer, member, addTrainerRequest.getCenterName(), addTrainerRequest.getRegistrationCount());
        relationshipRepository.save(relationship);

        return new AddTrainerResponse(trainer.getName(), relationship.getCenterName(), relationship.getRegistrationCount(), relationship.getRegistrationCount());
    }

    private User validateRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        if (user.isTrainer()) {
            throw new BusinessException(ErrorCode.REGISTER_ONLY_FOR_MEMBER);
        }
        return user;
    }

    private User validateUserCode(String userCode) {
        return userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER_CODE));
    }

    private void validateDuplicate(User member, User trainer) {
        Optional<Relationship> optionalRelationship = relationshipRepository.findByMemberAndTrainer(member, trainer);
        if (optionalRelationship.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_RELATIONSHIP);
        }
    }
}
