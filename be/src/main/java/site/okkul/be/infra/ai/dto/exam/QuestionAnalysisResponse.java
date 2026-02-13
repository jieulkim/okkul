package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// 개별 문항 분석 응답
public record QuestionAnalysisResponse(
		// 점수 및 텍스트 피드백
		@JsonProperty("grammar_score")
		Integer grammarScore,

		@JsonProperty("vocab_score")
		Integer vocabScore,

		@JsonProperty("logic_score")
		Integer logicScore,

		@JsonProperty("fluency_score")
		Integer fluencyScore,

		@JsonProperty("relevance_score")
		Integer relevanceScore,

		// AI 개선 문장
		@JsonProperty("improved_answer")
		String improvedAnswer,

		// AI 피드백 내용
		@JsonProperty("logic_feedback")
		String logicFeedback,

		@JsonProperty("fluency_feedback")
		String fluencyFeedback,

		@JsonProperty("relevance_feedback")
		String relevanceFeedback,

		// 문장별 상세 교정 (ElementCollection 매핑)
		@JsonProperty("sentence_feedbacks")
		List<SentenceFeedbackDto> sentenceFeedbacks
) {
}
