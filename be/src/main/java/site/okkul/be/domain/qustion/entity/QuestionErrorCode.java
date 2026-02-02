package site.okkul.be.domain.qustion.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode {

	QUESTION_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_001", "문항 세트를 찾을 수 없습니다."),
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_002", "문항을 찾을 수 없습니다."),
	TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_003", "주제를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
