package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "유형별 연습 피드백 저장 응답 DTO")
public class PracticeFeedbackResponse {

    @Schema(description = "연습 ID", example = "1")
    private Long practiceId;

    @Schema(description = "문제 ID", example = "101")
    private Long questionId;

    @Schema(description = "AI 피드백 결과")
    private PracticeAIFeedbackResult feedbackResult;
}