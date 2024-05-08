package funfit.pt.relationship.service;

import funfit.pt.rabbitMq.service.UserService;
import funfit.pt.rabbitMq.dto.ResponseValidateTrainerCode;
import funfit.pt.rabbitMq.dto.UserDto;
import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.dto.AddTrainerRequest;
import funfit.pt.relationship.dto.AddTrainerResponse;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
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
    private final UserService userService;

    public AddTrainerResponse addTrainer(AddTrainerRequest addTrainerRequest, HttpServletRequest request) {
        UserDto member = userService.getUserDto(jwtUtils.getEmailFromHeader(request));
        validateRole(member);

        ResponseValidateTrainerCode trainerDto = userService.getTrainerDto(addTrainerRequest.getUserCode());
        validateDuplicate(member.getUserId(), trainerDto.getTrainerUserId());

        Relationship relationship = Relationship.create(member.getUserId(), trainerDto.getTrainerUserId(),
                addTrainerRequest.getCenterName(), addTrainerRequest.getRegistrationCount());
        relationshipRepository.save(relationship);

        return new AddTrainerResponse(trainerDto.getUserName(), relationship.getCenterName(), relationship.getRegistrationCount(), relationship.getRegistrationCount());
    }

    private void validateRole(UserDto user) {
        if (!user.getRoleName().equals("회원")) {
            throw new BusinessException(ErrorCode.REGISTER_ONLY_FOR_MEMBER);
        }
    }

    private void validateDuplicate(long memberUserId, long trainerUserId) {
        Optional<Relationship> optionalRelationship = relationshipRepository.findByMemberAndTrainer(memberUserId, trainerUserId);
        if (optionalRelationship.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_RELATIONSHIP);
        }
    }
}
