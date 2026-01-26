package site.okkul.be.domain.qustion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 문항 요청 정보")
public record QuestionRequest(
		@Schema(description = "질문 텍스트", example = "Tell me about your favorite park.")
		String questionText,

		@Schema(description = "질문 음성 파일 URL", example = "https://cdn.okkul.site/audio/q1.mp3")
		String audioUrl,

		@Schema(description = "세트 내 출력 순서", example = "1")
		Integer order
) {
}
