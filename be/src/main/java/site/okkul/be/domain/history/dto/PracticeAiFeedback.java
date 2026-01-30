package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.okkul.be.domain.practice.entity.FeedbackStatus;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 피드백 상세 정보")
public class PracticeAiFeedback {

    @Schema(description = "피드백 상태", example = "COMPLETED")
    private FeedbackStatus status;

    @Schema(description = "AI가 제안한 더 좋은 답변", example = "I enjoy traveling.")
    private String improvedAnswer;

    @Schema(description = "유창성 피드백", example = "전반적으로 자연스럽습니다.")
    private String fluencyFeedback;

    @Schema(description = "논리성 피드백", example = "주장이 명확합니다.")
    private String logicFeedback;

    @Schema(description = "적합성 피드백", example = "주제에 맞는 답변입니다.")
    private String relevanceFeedback;

    @Schema(description = "문장별 상세 피드백 리스트")
    private List<PracticeSentenceFeedbackResponse> sentenceDetails;
}
