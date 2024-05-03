package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseValidateTrainerCode {

    private boolean result;
    private long trainerUserId;
    private String userName;
    private String trainerCode;

    public boolean getResult() {
        return result;
    }
}
