package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "시험 진행 상태 응답")
public record ExamStatusResponse(
		@Schema(description = "완료된 문항 수")
		int completedQuestions,
		@Schema(description = "총 문항 수")
		int totalQuestions,
		@Schema(description = "모든 분석 완료 여부")
		boolean isAllAnalyzed,
		@Schema(description = "예상 남은 시간(초)")
		int estimatedRemainingSeconds
) {
}
