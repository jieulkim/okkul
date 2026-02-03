package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// 개별 문항 분석 응답
public record QuestionAnalysisResponse(
		// 점수 및 텍스트 피드백
		@JsonProperty("grammarScore")
		Integer grammarScore,

		@JsonProperty("vocabScore")
		Integer vocabScore,

		@JsonProperty("logicScore")
		Integer logicScore,

		@JsonProperty("fluencyScore")
		Integer fluencyScore,

		@JsonProperty("relevanceScore")
		Integer relevanceScore,

		// AI 개선 문장
		@JsonProperty("improvedAnswer")
		String improvedAnswer,

		// AI 피드백 내용
		@JsonProperty("logicFeedback")
		String logicFeedback,

		@JsonProperty("fluencyFeedback")
		String fluencyFeedback,

		@JsonProperty("relevanceFeedback")
		String relevanceFeedback,

		// 문장별 상세 교정 (ElementCollection 매핑)
		@JsonProperty("sentenceFeedbacks")
		List<SentenceFeedbackDto> sentenceFeedbacks
) {
}
