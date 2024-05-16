package funfit.pt.diary.repository;

import funfit.pt.diary.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
