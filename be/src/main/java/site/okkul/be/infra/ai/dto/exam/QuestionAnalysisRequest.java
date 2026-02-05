package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.question.entity.Question;

/**
 * AI 서버에 개별 문항 분석을 요청하는 DTO
 */
public record QuestionAnalysisRequest(
		// 1. 분석의 핵심 대상
		@JsonProperty("user_answer")
		String userAnswer,       // 사용자가 말한/작성한 텍스트 (ExamAnswer.user_answer)

		// 2. 채점 기준이 되는 컨텍스트 (주제 적합성 판단용)
		@JsonProperty("original_question")
		String originalQuestion, // 실제 출제된 문제 지문

		// 3. (선택사항) 발음/유창성 분석용
		@JsonProperty("audio_url")
		String audioUrl,         // S3에 저장된 음성 파일 URL (ExamAnswer.audio_url)

		// 4. 추가 컨텍스트
		@JsonProperty("question_type")
		String questionType,     // 문제 유형 (묘사, 경험, 롤플레이 등 - AI의 피드백 방향성 결정)

		@JsonProperty("difficulty")
		Integer difficulty,      // 설정된 난이도

		// 5. 문항 순서 (AI 서버 요청 스펙에 맞춰 추가)
		@JsonProperty("question_order")
		Integer questionOrder
) {
	public static QuestionAnalysisRequest from(Question question, ExamAnswer answer) {
		return new QuestionAnalysisRequest(
				answer.getUserAnswer(),
				question.getQuestionText(),
				answer.getAudioUrl(),
				question.getQuestionSet().getQuestionType().getTypeCode(),
				question.getQuestionSet().getLevel(),
				answer.getId().getQuestionOrder()
		);
	}
}
