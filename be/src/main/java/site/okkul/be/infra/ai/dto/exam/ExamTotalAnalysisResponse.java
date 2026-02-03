package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 모의고사 전체 리포트 응답 DTO
 */
public record ExamTotalAnalysisResponse(
		@JsonProperty("predicted_level")
		String predictedLevel,      // 예상 등급 (예: "IH")

		@JsonProperty("total_score")
		Integer totalScore,         // 전체 점수 (예: 83)

		@JsonProperty("overall_evaluation")
		String overallEvaluation,   // 총평 텍스트 (UI 우측 상단)

		// 강점 및 개선 필요 항목 (UI 우측 하단 배지)
		@JsonProperty("strengths")
		List<String> strengths,     // 예: ["인물 및 장소 묘사"]

		@JsonProperty("improvements")
		List<String> improvements,  // 예: ["롤플레이 및 대안 제시"]

		// 레이더 차트용 5종 평균 점수 (Hard-coded)
		@JsonProperty("average_grammar_score")
		Integer averageGrammarScore,   // 문법 평균

		@JsonProperty("average_vocab_score")
		Integer averageVocabScore,     // 어휘 평균

		@JsonProperty("average_logic_score")
		Integer averageLogicScore,     // 논리 평균

		@JsonProperty("average_fluency_score")
		Integer averageFluencyScore,   // 유창성 평균

		@JsonProperty("average_relevance_score")
		Integer averageRelevanceScore  // 주제 적합성 평균
) {
}
