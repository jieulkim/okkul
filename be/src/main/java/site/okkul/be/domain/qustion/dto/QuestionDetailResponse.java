package site.okkul.be.domain.qustion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import site.okkul.be.domain.qustion.entity.Question;

@Schema(description = "개별 문항 상세 정보")
public record QuestionDetailResponse(
		@Schema(description = "문항 ID", example = "1")
		Long id,

		@Schema(description = "질문 텍스트", example = "Tell me about your house.")
		String questionText,

		@Schema(description = "질문 음성 파일 URL", example = "https://cdn.okkul.site/audio/q1.mp3")
		String audioUrl,

		@Schema(description = "세트 내 출력 순서", example = "1")
		Integer order
) {
	public static QuestionDetailResponse from(Question entity) {
		return new QuestionDetailResponse(
				entity.getId(),
				entity.getQuestionText(),
				entity.getAudioUrl(),
				entity.getOrder()
		);
	}
}
