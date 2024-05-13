package funfit.pt.relationship.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public void createRelationship(long memberId, long trainerId, String centerName, int registrationCount) {
        validateDuplicate(memberId, trainerId);

        Relationship relationship = Relationship.create(memberId, trainerId, centerName, registrationCount);
        relationshipRepository.save(relationship);
    }

    private void validateDuplicate(long memberId, long trainerId) {
        if (relationshipRepository.findByMemberAndTrainer(memberId, trainerId).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_RELATIONSHIP);
        }
    }
}
