package funfit.pt.relationship.repository;

import funfit.pt.relationship.entity.Relationship;
import funfit.pt.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    @Query("select r from Relationship r " +
            "where r.member = :member " +
            "and r.trainer = :trainer")
    Optional<Relationship> findByMemberAndTrainer(@Param("member")User member, @Param("trainer") User trainer);

    Slice<Relationship> findSliceByTrainer(User trainer, Pageable pageable);
}
