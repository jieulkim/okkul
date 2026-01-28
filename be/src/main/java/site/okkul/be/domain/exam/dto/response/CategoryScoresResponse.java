package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오각형 차트용 데이터")
public record CategoryScoresResponse(
		@Schema(description = "문법 점수", example = "80")
		Integer grammar,

		@Schema(description = "어휘 점수", example = "75")
		Integer vocabulary,

		@Schema(description = "논리 점수", example = "90")
		Integer logic,

		@Schema(description = "유창성 점수", example = "85")
		Integer fluency,

		@Schema(description = "주제 적합성 점수", example = "70")
		Integer relevance
) {
}
