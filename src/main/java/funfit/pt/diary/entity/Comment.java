package funfit.pt.diary.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private long id;

    @Column(nullable = false)
    private String writerEmail;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static Comment create(String writerEmail, String content) {
        Comment comment = new Comment();
        comment.writerEmail = writerEmail;
        comment.content = content;
        return comment;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
