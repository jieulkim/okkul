package site.okkul.be.domain.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 모의고사 전체 결과에 대한 종합 분석 리포트 엔티티
 */
@Entity
@Table(name = "exam_report")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExamReport {

    @Id
    @Column(name = "exam_id")
    private Long examId;

    /**
     * [오각형 차트용] 영역별 평균 점수 (0.00 ~ 100.00)
     * 15개 문항의 각 항목 점수를 합산하여 평균을 낸 값입니다.
     */
    @Column(name = "avg_grammar", precision = 5, scale = 2)
    private BigDecimal avgGrammar;

    @Column(name = "avg_vocab", precision = 5, scale = 2)
    private BigDecimal avgVocab;

    @Column(name = "avg_logic", precision = 5, scale = 2)
    private BigDecimal avgLogic;

    @Column(name = "avg_fluency", precision = 5, scale = 2)
    private BigDecimal avgFluency;

    @Column(name = "avg_relevance", precision = 5, scale = 2)
    private BigDecimal avgRelevance;

    /**
     * [최종 판단용] 전체 평균 점수 및 등급
     * 위 4개 영역의 점수를 종합 가중치로 계산한 최종 점수입니다.
     */
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    /**
     * 오픽 예측 등급 (AL, IH, IM1, IM2, IM3, IL 등)
     */
    @Column(name = "grade")
    private String grade;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * 가장 점수가 높은 강점 영역 (예: "Grammar", "Fluency")
     */
    @Column(name = "strength_type", length = 100)
    private String strengthType;

    /**
     * 가장 점수가 낮은 약점 영역 (예: "Vocabulary", "Logic")
     */
    @Column(name = "weakness_type", length = 100)
    private String weaknessType;

    /**
     * 리포트 생성 일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ExamReport createReport(Long examId, BigDecimal avgG, BigDecimal avgV, BigDecimal avgL, BigDecimal avgF, BigDecimal avgR, BigDecimal total, String grade, String sType, String wType, String comment) {
        return ExamReport.builder()
                .examId(examId)
                .avgGrammar(avgG)
                .avgVocab(avgV)
                .avgLogic(avgL)
                .avgFluency(avgF)
                .avgRelevance(avgR)
                .totalScore(total)
                .grade(grade)
                .strengthType(sType)
                .weaknessType(wType)
                .comment(comment)
                .createdAt(Instant.now())
                .build();
    }
}
