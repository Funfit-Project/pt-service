package funfit.pt.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public class CreateNewMemberSubDto implements Serializable {

    private long memberId;
    private long trainerId;
    private String centerName;
    private int registrationCount;
}
