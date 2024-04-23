package funfit.pt.relationship.entity;

import funfit.pt.BaseEntity;
import funfit.pt.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Relationship extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_user_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_user_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private String centerName;

    @Column(nullable = false)
    private int registrationCount;

    @Column(nullable = false)
    private int remainingCount;

    public static Relationship create(User trainer, User member, String centerName, int registrationCount) {
        Relationship relationship = new Relationship();
        relationship.trainer = trainer;
        relationship.member = member;
        relationship.centerName = centerName;
        relationship.registrationCount = registrationCount;
        relationship.remainingCount = registrationCount;
        return relationship;
    }
}
