package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.okkul.be.domain.exam.entity.ExamAnswer;

public record AnswerSummaryDto(
		@JsonProperty("question_order")
		Integer questionOrder,   // 문항 순서

		@JsonProperty("stt_script")
		String userAnswer,       // 사용자의 원본 답변 (흐름 파악용)

		@JsonProperty("improved_answer")
		String improvedAnswer,   // AI 교정 모범 답안

		// 개별 문항에서 이미 계산된 점수들 (통계 및 취약점 분석용)
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

		@JsonProperty("logic_feedback")
		String logicFeedback     // 문항별 개별 피드백 내용
) {
	public static AnswerSummaryDto from(ExamAnswer answer) {
		return new AnswerSummaryDto(
				answer.getId().getQuestionOrder(),
				answer.getUserAnswer(),
				answer.getImprovedAnswer(),
				answer.getGrammarScore(),
				answer.getVocabScore(),
				answer.getLogicScore(),
				answer.getFluencyScore(),
				answer.getRelevanceScore(),
				answer.getLogicFeedback()
		);
	}
}
