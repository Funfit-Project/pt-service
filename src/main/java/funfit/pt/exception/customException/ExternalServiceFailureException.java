package funfit.pt.exception.customException;

import funfit.pt.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExternalServiceFailureException extends RuntimeException {

    private final ErrorCode errorCode;
}
