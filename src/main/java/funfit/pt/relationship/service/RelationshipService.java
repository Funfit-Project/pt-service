package funfit.pt.relationship.service;

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
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public void createRelationship(String memberEmail, String trainerEmail, String centerName, int registrationCount) {
        validateDuplicate(memberEmail, trainerEmail);

        Relationship relationship = Relationship.create(memberEmail, trainerEmail, centerName, registrationCount);
        relationshipRepository.save(relationship);
    }

    private void validateDuplicate(String memberEmail, String trainerEmail) {
        if (relationshipRepository.findByMemberAndTrainerEmail(memberEmail, trainerEmail).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_RELATIONSHIP);
        }
    }
}
