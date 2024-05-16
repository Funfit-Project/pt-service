package funfit.pt.diary.entity;

import funfit.pt.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private long id;

    @Column(nullable = false)
    private String writerEmail;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public static Post create(String writerEmail, String content, Category category) {
        Post post = new Post();
        post.writerEmail = writerEmail;
        post.content = content;
        post.category = category;
        return post;
    }

    // 연관관계 편의 메서드
    public void addComment(Comment comment) {
        this.getComments().add(comment);
        comment.setPost(this);
    }

    // 연관관계 편의 메서드
    public void addImage(Image image) {
        this.getImages().add(image);
        image.setPost(this);
    }
}
