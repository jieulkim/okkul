package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 분석 진행 상태 응답")
public class ExamStatusResponse {

    @Schema(description = "현재 분석 완료된 문항 수", example = "12")
    private Integer completedQuestions;

    @Schema(description = "전체 문항 수", example = "15")
    private Integer totalQuestions;

    @Schema(description = "전체 분석 완료 여부", example = "false")
    private boolean isAllAnalyzed;

    @Schema(description = "예상 남은 시간 (초)", example = "10")
    private Integer estimatedRemainingSeconds;
}
