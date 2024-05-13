package funfit.pt.relationship.repository;

import funfit.pt.relationship.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org. springframework.data.repository.query.Param;

import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    @Query("select r from Relationship r " +
            "where r.memberEmail = :memberEmail " +
            "and r.trainerEmail = :trainerEmail")
    Optional<Relationship> findByMemberAndTrainerEmail(@Param("memberEmail") String memberEmail,
                                                       @Param("trainerEmail") String trainerEmail);

    @Query("select r from Relationship r where r.memberEmail = :memberEmail")
    Optional<Relationship> findByMemberEmail(@Param("memberEmail") String memberEmail);

//    Slice<Relationship> findSliceByTrainer(User trainer, Pageable pageable);
}
