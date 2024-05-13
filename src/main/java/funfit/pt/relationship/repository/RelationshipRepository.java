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
            "where r.memberId = :memberId " +
            "and r.trainerId = :trainerId")
    Optional<Relationship> findByMemberAndTrainer(@Param("memberId") long memberId,
                                                  @Param("trainerId") long trainerId);

//    Slice<Relationship> findSliceByTrainer(User trainer, Pageable pageable);
}
