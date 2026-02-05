package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 총체 리포트(총평) 응답")
public class ExamReportResponse {

    @Schema(description = "모의고사 ID", example = "12")
    private Long examId;

    @Schema(description = "전체 점수(0~100)", example = "78.50")
    private BigDecimal totalScore;

    @Schema(description = "예측 등급", example = "IM")
    private String grade;

    @Schema(description = "문법 평균 점수", example = "76.20")
    private BigDecimal avgGrammar;

    @Schema(description = "어휘 평균 점수", example = "72.10")
    private BigDecimal avgVocab;

    @Schema(description = "논리 평균 점수", example = "74.00")
    private BigDecimal avgLogic;

    @Schema(description = "유창성 평균 점수", example = "68.50")
    private BigDecimal avgFluency;

    @Schema(description = "주제 적합성 평균 점수", example = "80.30")
    private BigDecimal avgRelevance;

    @Schema(description = "전체 총평 내용", example = "문장 구조는 안정적이지만 질문 의도 반영이 약합니다.")
    private String comment;

    @Schema(description = "전체적인 강점 유형 리스트")
    private List<String> strengthTypes;

    @Schema(description = "전체적인 약점 유형 리스트")
    private List<String> weaknessTypes;

    @Schema(description = "리포트 생성 일시", example = "2026-01-29T09:26:10")
    private LocalDateTime createdAt;
}
