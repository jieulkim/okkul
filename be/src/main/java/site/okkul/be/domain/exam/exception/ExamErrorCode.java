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
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM_003", "문항을 찾을 수 없습니다."),
	QUESTION_ALLOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EXAM_004", "문제 할당에 실패했습니다."),
	UNKNOWN_QUESTION_TYPE(HttpStatus.BAD_REQUEST, "EXAM_005", "알 수 없는 질문 유형 코드입니다."),
	INVALID_DIFFICULTY_VALUE(HttpStatus.BAD_REQUEST, "EXAM_006", "유효하지 않은 난이도 값입니다."),
	EXAM_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "EXAM_007", "이미 종료된 모의고사입니다."),
	EXAM_ANSWER_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "EXAM_008", "이미 제출된 답변입니다."),
	EXAM_REPORT_ALREADY_CREATED(HttpStatus.BAD_REQUEST, "EXAM_009", "이미 리포트가 생성된 시험입니다."),
	AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXAM_010", "AI 서버 오류입니다"),
	;


	private final HttpStatus status;
	private final String code;
	private final String message;
}
