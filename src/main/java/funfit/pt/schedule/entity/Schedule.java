package funfit.pt.schedule.entity;

import funfit.pt.relationship.entity.Relationship;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"date_time", "trainer_email"})})
public class Schedule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id")
    private Relationship relationship;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String trainerEmail;

    public static Schedule create(Relationship relationship, LocalDateTime dateTime) {
        Schedule schedule = new Schedule();
        schedule.relationship = relationship;
        schedule.dateTime = dateTime;
        schedule.trainerEmail = relationship.getTrainerEmail();
        return schedule;
    }
}
