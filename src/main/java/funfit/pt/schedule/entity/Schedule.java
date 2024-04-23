package funfit.pt.schedule.entity;

import funfit.pt.relationship.entity.Relationship;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Schedule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Relationship relationship;

    @Column(nullable = false)
    private LocalDateTime date;

    private String memo;

    private String review;

    public static Schedule create(Relationship relationship, LocalDateTime date, String memo) {
        Schedule schedule = new Schedule();
        schedule.relationship = relationship;
        schedule.date = date;
        schedule.memo = memo;
        return schedule;
    }
}
