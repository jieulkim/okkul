package site.okkul.be.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "오각형 차트 데이터 (각 0~100점 기준)")
public class CategoryScoresResponse {
    @Schema(description = "문법 점수", example = "80")
    private Integer grammar;

    @Schema(description = "어휘 점수", example = "75")
    private Integer vocabulary;

    @Schema(description = "논리 점수", example = "90")
    private Integer logic;

    @Schema(description = "유창성 점수", example = "85")
    private Integer fluency;

    @Schema(description = "주제 적합성 점수", example = "70")
    private Integer relevance;
}