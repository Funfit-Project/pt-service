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
    private String memberEmail;

    @Column(nullable = false)
    private String trainerEmail;

    @Column(nullable = false)
    private String centerName;

    @Column(nullable = false)
    private int registrationCount;

    @Column(nullable = false)
    private int remainingCount;

    public static Relationship create(String memberEmail, String trainerEmail, String centerName, int registrationCount) {
        Relationship relationship = new Relationship();
        relationship.memberEmail = memberEmail;
        relationship.trainerEmail = trainerEmail;
        relationship.centerName = centerName;
        relationship.registrationCount = registrationCount;
        relationship.remainingCount = registrationCount;
        return relationship;
    }
}
