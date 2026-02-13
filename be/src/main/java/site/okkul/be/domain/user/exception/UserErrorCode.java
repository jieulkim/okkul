package site.okkul.be.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
	INVALID_OPIC_LEVEL(HttpStatus.BAD_REQUEST, "USER_002", "유효하지 않은 OPIc 등급입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
