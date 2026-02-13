package site.okkul.be.global.exception;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.okkul.be.infra.alarm.AlarmService;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final AlarmService alarmService;

	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		// 인터페이스 덕분에 어떤 도메인 에러가 와도 한 줄로 처리 가능!
		return ErrorResponse.toResponseEntity(e.getErrorCode());
	}

	@ExceptionHandler(SystemException.class)
	protected ResponseEntity<ErrorResponse> handleSystemException(SystemException e, HttpServletRequest request) {
		alarmService.sendMessage(e.getTitle(), e.getMessage(), getFullPath(request));
		return ErrorResponse.toResponseEntity(e.getErrorCode());
	}

	// Validation 실패 시 발생 (400)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 입력값입니다: " + e.getMessage());
	}

	// 비즈니스 로직 상 잘못된 요청일 때 (400)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	// 그 외 예상치 못한 모든 에러 (500)
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleAllException(Exception e, HttpServletRequest request) {
		log.error("Unexpected Exception: ", e);

		// 2. 알람 서비스에 전달
		alarmService.sendErrorAlarm(e, getFullPath(request));

		return ErrorResponse.toResponseEntity(GlobalErrorCode.INTERNAL_SERVER_ERROR);
	}

	private String getFullPath(HttpServletRequest request) {
		String requestUrl = request.getRequestURL().toString();
		String method = request.getMethod();
		return String.format("[%s] %s", method, requestUrl);
	}
}
