package site.okkul.be.domain.practice.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import site.okkul.be.domain.practice.entity.FeedbackStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "AI 피드백 결과 상세")
public class PracticeAIFeedbackResult {

    @Schema(description = "피드백 응답 상태 반환")
    private FeedbackStatus feedbackStatus;

    @Schema(description = "문법 및 어휘 교정 제안 목록")
    private List<SentenceCorrection> scriptCorrections;

    @Schema(description = "주제 적합성 피드백", example = "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다.")
    private String relevanceFeedback;

    @Schema(description = "논리성 피드백", example = "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다.")
    private String logicFeedback;

    @Schema(description = "유창성 피드백", example = "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다.")
    private String fluencyFeedback;

    @Schema(description = "AI 개선 종합 답변", example = "That's my ~~")
    private String aiImprovedAnswer;

    public PracticeAIFeedbackResult(FeedbackStatus feedbackStatus) {
        this.feedbackStatus = feedbackStatus;
    }
}
