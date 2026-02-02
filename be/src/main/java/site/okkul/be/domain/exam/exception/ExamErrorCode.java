package site.okkul.be.domain.exam.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ExamErrorCode implements ErrorCode {

	EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM_001", "시험 세션을 찾을 수 없습니다."),
	SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM_002", "연관된 설문 데이터를 찾을 수 없습니다."),
	QUESTION_ALLOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EXAM_003", "문제 할당에 실패했습니다."),
	UNKNOWN_QUESTION_TYPE(HttpStatus.BAD_REQUEST, "EXAM_004", "알 수 없는 질문 유형 코드입니다."),
	INVALID_DIFFICULTY_VALUE(HttpStatus.BAD_REQUEST, "EXAM_005", "유효하지 않은 난이도 값입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
