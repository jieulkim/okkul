package site.okkul.be.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import site.okkul.be.domain.exam.entity.AnswerStatus;
import site.okkul.be.domain.question.entity.Question;

/**
 * 유저가 사용할 퀘스트 반환형
 */
@Schema(name = "QuestionResponse", description = "사용자에게 반환되는 퀘스트(문제) 정보")
public record ExamQuestionResponse(
		@Schema(
				description = "문제 ID",
				example = "1")
		Long questionId,

		@Schema(
				description = "문제 텍스트",
				example = "다음 중 올바른 것은 무엇인가요?"
		)
		String questionText,

		@Schema(
				description = "문제에 대한 오디오 파일 URL",
				example = "https://example.com/audio/1.mp3"
		)
		String audioUrl,

		@Schema(
				description = "모의고사 문제 순서 (Answer 저장 시 복합키로 활용됨)",
				example = "1"
		)
		Integer order,
		AnswerStatus answerStatus
) {
	public static ExamQuestionResponse from(Question question, AnswerStatus answerStatus, Integer order) {
		return new ExamQuestionResponse(
				question.getId(),
				question.getQuestionText(),
				question.getAudioUrl(),
				order,
				answerStatus
		);
	}
}
