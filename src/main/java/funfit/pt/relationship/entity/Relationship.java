package funfit.pt.relationship.entity;

import funfit.pt.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Relationship extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private long id;

    @Column(nullable = false)
    private long memberId;

    @Column(nullable = false)
    private long trainerId;

    @Column(nullable = false)
    private String centerName;

    @Column(nullable = false)
    private int registrationCount;

    @Column(nullable = false)
    private int remainingCount;

    public static Relationship create(long memberId, long trainerId, String centerName, int registrationCount) {
        Relationship relationship = new Relationship();
        relationship.memberId = memberId;
        relationship.trainerId = trainerId;
        relationship.centerName = centerName;
        relationship.registrationCount = registrationCount;
        relationship.remainingCount = registrationCount;
        return relationship;
    }
}
