package funfit.pt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // external service
    UNAVAILABLE_AUTH_SERVICE(HttpStatus.SERVICE_UNAVAILABLE, "현재 auth 서비스는 사용 불가입니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 데이터를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),

    // not found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // user
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    NOT_FOUND_EMAIL(HttpStatus.BAD_REQUEST, "가입되지 않은 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 패스워드입니다."),

    // relationship
    DUPLICATED_RELATIONSHIP(HttpStatus.BAD_REQUEST, "이미 등록된 관계입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "잘못된 사용자 역할입니다."),
    NOT_FOUND_RELATIONSHIP_ID(HttpStatus.BAD_REQUEST, "해당 id에 대한 relationship을 찾을 수 없습니다."),

    // schedule
    ALREADY_RESERVATION(HttpStatus.BAD_REQUEST, "이미 예약된 시간입니다."),

    // post
    UNAUTHORIZED_CREATE_PT_LOG(HttpStatus.UNAUTHORIZED, "수업일지는 트레이너만 작성할 수 있습니다."),
    UNAUTHORIZED_CREATE_DIARY(HttpStatus.UNAUTHORIZED, "다이어리는 회원만 작성할 수 있습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 카테고리입니다."),
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),


    // jwt
    EXPIRED_JWT(HttpStatus.BAD_REQUEST, "만료된 토큰입니다."),
    INVALID_JWT(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    REQUIRED_JWT(HttpStatus.BAD_REQUEST, "토큰이 입력되지 않았습니다."),

    // unauthorized
    ONLY_TRAINER(HttpStatus.UNAUTHORIZED, "트레이너만 접근 가능합니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
