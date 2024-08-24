package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseValidateTrainerCode implements Serializable {

    private boolean result;
    private long trainerUserId;
    private String userName;
    private String trainerCode;

    public boolean getResult() {
        return result;
    }
}
