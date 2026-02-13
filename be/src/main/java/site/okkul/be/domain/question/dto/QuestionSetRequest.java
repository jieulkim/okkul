package site.okkul.be.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문항 세트 생성/수정 요청")
public record QuestionSetRequest(
		@Schema(description = "난이도 (1~6)", example = "3")
		Integer level,

		@Schema(description = "연관 토픽 ID", example = "101")
		Long topicId,

		@Schema(description = "문제 유형 ID", example = "1")
		Long typeId
) {
}
