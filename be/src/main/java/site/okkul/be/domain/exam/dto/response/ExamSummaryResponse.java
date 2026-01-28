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
public class ExamSummaryResponse {
    @Schema(description = "전체 점수 (0-100)", example = "85.5")
    private Double totalScore;

    @Schema(description = "예측 등급", example = "IH")
    private String grade;

    @Schema(description = "오각형 차트용 카테고리별 점수")
    private CategoryScoresResponse categoryScores;

    @Schema(description = "전체 총평 내용")
    private String comment;

    @Schema(description = "전체적인 강점 유형")
    private String strengths;

    @Schema(description = "전체적인 약점 유형")
    private String weakness;
}
