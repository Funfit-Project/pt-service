package funfit.pt.rabbitMq.entity;

import lombok.*;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private long userId;
    private String email;
    private String userName;
    private Role role;
    private String phoneNumber;
    private String userCode;
}
