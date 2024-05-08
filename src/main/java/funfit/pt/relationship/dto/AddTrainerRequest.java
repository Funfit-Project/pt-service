package funfit.pt.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainerRequest {

    private String userCode;
    private String centerName;
    private int registrationCount;
}
