package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class PracticeStartResponse {
    @Schema(description = "생성된 연습의 ID", example = "1")
    private Long practiceId;

    @Schema(description = "유형 연습 시작 날짜", example = "2024-01-22T12:30:00Z")
    private Instant createdAt;
}
