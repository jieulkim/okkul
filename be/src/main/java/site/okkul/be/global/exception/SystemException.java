package site.okkul.be.global.exception;

import lombok.Getter;

/**
 * 관리자에게 알림을 보내는 동시에 유저에게 에러 코드를 정보를 반환합니다
 * (유저한테는 관리자용 메시지는 안보임)
 */
@Getter
public class SystemException extends BusinessException {
	private final String title;
	private final String message; // 관리자용 상세 메시지

	public SystemException(ErrorCode errorCode, String title, String message) {
		super(errorCode);
		this.title = title;
		this.message = message;
	}
}
