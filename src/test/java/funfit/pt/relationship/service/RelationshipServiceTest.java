package funfit.pt.relationship.service;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import funfit.pt.relationship.entity.Relationship;
import funfit.pt.relationship.repository.RelationshipRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Slf4j
class RelationshipServiceTest {

    @Autowired
    private RelationshipService relationshipService;
    @Autowired
    private RelationshipRepository relationshipRepository;

    @Test
    @DisplayName("relationship 생성 성공")
    void createRelationshipSuccess() {
        // given
        long memberId = 1l;
        long trainerId = 2l;
        relationshipService.createRelationship(memberId, trainerId, "석세스짐", 10);

        // then
        Assertions.assertThat(relationshipRepository.findByMemberAndTrainer(memberId, trainerId))
                .isPresent();
    }

    @Test
    @DisplayName("relationship 생성 실패-이미 생성된 relationship")
    void createRelationshipFailByDuplicated() {
        // given
        long memberId = 1l;
        long trainerId = 2l;
        relationshipRepository.save(Relationship.create(memberId, trainerId, "석세스짐", 10));

        // then
        Assertions.assertThat(assertThrows(BusinessException.class,
                        () -> relationshipService.createRelationship(memberId, trainerId, "석세스짐", 10)).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATED_RELATIONSHIP);
    }
}
