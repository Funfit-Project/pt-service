package funfit.pt.relationship.entity;

import funfit.pt.responseDto.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Relationship extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private long id;

    @Column(nullable = false)
    private long memberUserId;

    @Column(nullable = false)
    private long trainerUserId;

    @Column(nullable = false)
    private String centerName;

    @Column(nullable = false)
    private int registrationCount;

    @Column(nullable = false)
    private int remainingCount;

    public static Relationship create(long memberUserId, long trainerUserId, String centerName, int registrationCount) {
        Relationship relationship = new Relationship();
        relationship.memberUserId = memberUserId;
        relationship.trainerUserId = trainerUserId;
        relationship.centerName = centerName;
        relationship.registrationCount = registrationCount;
        relationship.remainingCount = registrationCount;
        return relationship;
    }
}
