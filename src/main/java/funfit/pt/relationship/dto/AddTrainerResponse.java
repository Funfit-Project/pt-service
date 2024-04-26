package funfit.pt.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainerResponse {

    private String trainerName;
    private String centerName;
    private int registrationCount;
    private int remainingCount;
}
