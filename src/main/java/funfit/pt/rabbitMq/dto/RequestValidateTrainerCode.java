package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestValidateTrainerCode {

    private String trainerCode;
    private String responseQueue;
}
