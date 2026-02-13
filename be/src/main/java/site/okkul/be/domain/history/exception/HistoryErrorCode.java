package site.okkul.be.domain.history.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum HistoryErrorCode implements ErrorCode {

	EXAM_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_001", "해당 모의고사가 존재하지 않습니다."),
	EXAM_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_002", "해당 문항 답변이 존재하지 않습니다."),
	AI_ANALYSIS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "HISTORY_003", "AI 분석이 완료되지 않았습니다."),
	AI_IMPROVED_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_004", "AI 개선 답변이 존재하지 않습니다."),
	PRACTICE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_005", "해당 연습 기록을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
