package funfit.pt.api.dto;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Role {

    TRAINER("트레이너"),
    MEMBER("회원");

    private final String name;

    public static Role find(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.name.equals(name))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ROLE));
    }
}
