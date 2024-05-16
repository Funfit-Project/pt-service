package funfit.pt.diary.repository;

import funfit.pt.diary.entity.Post;
import funfit.pt.relationship.entity.Relationship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Slice<Post> findByRelationship(Relationship relationship, Pageable pageable);
}
