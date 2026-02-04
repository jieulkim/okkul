package site.okkul.be.infra.ai;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;
import site.okkul.be.infra.ai.dto.SentenceFeedbackResponse;
import site.okkul.be.infra.ai.dto.exam.AnswerSummaryDto;
import site.okkul.be.infra.ai.dto.exam.ExamTotalAnalysisResponse;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisRequest;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisResponse;
import site.okkul.be.infra.ai.dto.exam.SentenceFeedbackDto;

@Slf4j
@Component
public class FakeAiClient implements AiClient {
	@Override
	public AiFeedbackResponse requestFeedback(AiFeedbackRequest request) {
		// Return a predefined mock response for testing purposes
		return AiFeedbackResponse.builder()
				.improved_answer("This is a mock improved answer from the Fake AI client.")
				.relevance_feedback("Mock feedback on relevance: Your answer was very relevant.")
				.logic_feedback("Mock feedback on logic: The logic of your answer is sound.")
				.fluency_feedback("Mock feedback on fluency: You speak very fluently.")
				.sentence_details(Collections.singletonList(
						SentenceFeedbackResponse.builder()
								.target_sentence("This was the original sentence.")
								.target_text("original")
								.improved_text("improved")
								.feedback("This is a mock feedback on a specific sentence part.")
								.sentence_order(1)
								.build()
				))
				.build();
	}

	@Override
	public ResponseEntity<QuestionAnalysisResponse> analyzeQuestion(QuestionAnalysisRequest request) {
		log.info("FAKE 답변을 반환했습니다");
//		return ResponseEntity.badRequest().build();
		return ResponseEntity.ok(
				new QuestionAnalysisResponse(
						80,
						80,
						80,
						80,
						80,
						"This is a mock improved answer from the Fake AI client.",
						"Mock feedback on logic: The logic of your answer is sound.",
						"Mock feedback on fluency: You speak very fluently.",
						"Mock feedback on relevance: Your answer was very relevant.",
						List.of(
								new SentenceFeedbackDto(
										"This was the original sentence.",
										"original",
										"improved",
										"This is a mock feedback on a specific sentence part.",
										1
								)
						)
				)
		);
	}

	@Override
	public ResponseEntity<ExamTotalAnalysisResponse> analyzeTotalExam(List<AnswerSummaryDto> request) {
		// 간단한 Mock 응답 반환
		return ResponseEntity.ok(new ExamTotalAnalysisResponse(
				"IH", // predictedLevel
				83,   // totalScore
				"전반적으로 표현이 자연스럽고 논리 전개가 명확합니다. 일부 문장 연결과 세부 표현을 다듬으면 더 높은 점수를 기대할 수 있습니다.", // overallEvaluation
				List.of("인물 및 장소 묘사", "문법적 정확성"), // strengths
				List.of("롤플레이 및 대안 제시", "구체적 예시 부족"), // improvements
				80, // averageGrammarScore
				78, // averageVocabScore
				82, // averageLogicScore
				79, // averageFluencyScore
				81  // averageRelevanceScore
		));
	}
}
