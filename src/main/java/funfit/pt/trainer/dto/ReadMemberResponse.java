package funfit.pt.trainer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadMemberResponse {

    private String trainerName;
    private String memberName;
    private String centerName;
    private int registrationCount;
    private int remainingCount;
}
