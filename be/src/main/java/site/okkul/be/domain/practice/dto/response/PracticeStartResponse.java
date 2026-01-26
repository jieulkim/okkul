package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.okkul.be.domain.exam.dto.QuestionResponse;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class PracticeStartResponse {
    @Schema(description = "생성된 연습의 ID", example = "1")
    private Long practiceId;

    @Schema(description = "유형 연습 시작 날짜", example = "2024-01-22T12:30:00Z")
    private Instant createdAt;

    @Schema(description = "할당된 문제 세트의 ID", example = "10")
    private Long setId;

    @Schema(description = "연습에 포함된 질문 목록")
    private List<PracticeQuestion> questions;
}
