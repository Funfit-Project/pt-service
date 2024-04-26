package funfit.pt.relationship.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddTrainerRequest {

    private String userCode;
    private String centerName;
    private int registrationCount;
}
