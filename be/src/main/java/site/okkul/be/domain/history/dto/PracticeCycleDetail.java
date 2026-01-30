package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "연습 문항별 상세 사이클 정보 (질문 -> 답변 -> 피드백)")
public class PracticeCycleDetail {

    @Schema(description = "시도 순서 (1차 시도, 2차 시도...)", example = "1")
    private int attemptOrder;

    @Schema(description = "사용자 답변 정보")
    private PracticeAnswer userAnswer;

    @Schema(description = "AI 피드백 정보")
    private PracticeAiFeedback feedback;
}
