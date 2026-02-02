package site.okkul.be.domain.practice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PracticeErrorCode implements ErrorCode {
    PRACTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRACTICE_001", "진행중인 연습 세션을 찾을 수 없습니다."),
    PRACTICE_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "PRACTICE_002", "연습 답변 데이터를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
