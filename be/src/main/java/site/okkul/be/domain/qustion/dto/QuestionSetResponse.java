package site.okkul.be.domain.qustion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import site.okkul.be.domain.qustion.entity.QuestionSet;

@Schema(description = "문항 세트 응답 정보")
public record QuestionSetResponse(
		@Schema(description = "세트 ID", example = "10")
		Long setId,

		@Schema(description = "난이도 (1~6)", example = "4")
		Integer level,

		@Schema(description = "세트에 포함된 문항 수", example = "3")
		Integer questionCnt,

		@Schema(description = "토픽 이름", example = "영화 보기")
		String topicName,

		@Schema(description = "문제 유형 코드", example = "COMBO3")
		String typeCode,

		@Schema(description = "포함된 상세 문항 리스트")
		List<QuestionDetailResponse> questions,

		@Schema(description = "생성 일시")
		Instant createdAt
) {
	public static QuestionSetResponse from(QuestionSet entity) {
		return new QuestionSetResponse(
				entity.getId(),
				entity.getLevel(),
				entity.getQuestionCnt(),
				entity.getTopic().getTopicName(),
				entity.getQuestionType().getTypeCode(),
				entity.getQuestions().stream()
						.map(QuestionDetailResponse::from)
						.toList(),
				entity.getCreatedAt()
		);
	}
}
