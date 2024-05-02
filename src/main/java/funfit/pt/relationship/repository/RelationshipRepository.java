package funfit.pt.relationship.repository;

import funfit.pt.relationship.entity.Relationship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org. springframework.data.repository.query.Param;

import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    @Query("select r from Relationship r " +
            "where r.memberUserId = :memberUserId " +
            "and r.trainerUserId = :trainerUserId")
    Optional<Relationship> findByMemberAndTrainer(@Param("memberUserId") long memberUserId,
                                                  @Param("trainerUserId") long trainerUserId);

//    Slice<Relationship> findSliceByTrainer(User trainer, Pageable pageable);
}
