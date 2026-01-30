package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 히스토리 상세 응답 데이터")
public class ExamHistoryDetailResponse {
    private Long examId;
    private Instant createdAt;
    private Instant endAt;

    // 모의고사 총평
    private ExamReport examReport;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "모의고사 총체 리포트")
    public static class ExamReport {
        private BigDecimal totalScore;
        private String grade;
        private BigDecimal avgGrammar;
        private BigDecimal avgVocab;
        private BigDecimal avgLogic;
        private BigDecimal avgFluency;
        private BigDecimal avgRelevance;
        private String comment;
        private List<String> strengthTypes;
        private List<String> weaknessTypes;
        private Instant createdAt;
    }
}
