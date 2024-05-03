package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
