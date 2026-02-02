package site.okkul.be.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "개별 문항 요청 정보")
public record QuestionRequest(
		@Schema(description = "질문 텍스트", example = "Tell me about your favorite park.")
		@NotBlank
		String questionText,

		@Schema(description = "질문 음성 파일 URL", example = "https://cdn.okkul.site/audio/q1.mp3")
		@NotBlank
		String audioUrl,

		@Schema(description = "세트 내 출력 순서", example = "1")
		@NotNull
		Integer order
) {
}
