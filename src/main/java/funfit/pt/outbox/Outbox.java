package funfit.pt.outbox;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id", nullable = false)
    private long id;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String topic;

    public static Outbox create(String payload, String topic) {
        Outbox outbox = new Outbox();
        outbox.payload = payload;
        outbox.topic = topic;
        return outbox;
    }
}

