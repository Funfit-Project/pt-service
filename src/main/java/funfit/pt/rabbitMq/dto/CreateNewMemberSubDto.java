package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public class CreateNewMemberSubDto implements Serializable {

    private String memberEmail;
    private String trainerEmail;
    private String centerName;
    private int registrationCount;
}
