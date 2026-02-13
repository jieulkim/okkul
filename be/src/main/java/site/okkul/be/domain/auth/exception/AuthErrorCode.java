package site.okkul.be.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	TOKEN_INVALID_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_001", "토큰이 유효하지 않습니다"),
	USER_NOT_REGISTERED(HttpStatus.UNAUTHORIZED, "AUTH_002", "등록된 유저가 없습니다."),
	UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_003", "지원하지 않는 소셜 로그인 제공자입니다.");


	private final HttpStatus status;
	private final String code;
	private final String message;
}
