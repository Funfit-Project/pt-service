package funfit.pt.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PtMemberJoinedDto implements Serializable {

    private String memberEmail;
    private String trainerEmail;
    private String centerName;
    private int registrationCount;
}
