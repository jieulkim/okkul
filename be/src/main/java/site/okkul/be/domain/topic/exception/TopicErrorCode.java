package site.okkul.be.domain.topic.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum TopicErrorCode implements ErrorCode {
    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC_001", "주제를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
