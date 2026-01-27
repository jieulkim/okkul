package site.okkul.be.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 개별 답변에 대한 AI의 상세 피드백 정보 엔티티
 */
@Entity
@Table(name = "type_feedback")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TypeFeedback {

    @Id
    @Column(name = "answer_id")
    private Long answerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // answerId를 PK이자 FK로 사용
    @JoinColumn(name = "answer_id")
    private ExamAnswer examAnswer;

    // 0~100 사이의 점수
    @Column(name = "grammar_score")
    private Integer grammarScore;

    @Column(name = "grammar_feedback", columnDefinition = "TEXT")
    private String grammarFeedback;

    @Column(name = "vocab_score")
    private Integer vocabScore;

    @Column(name = "vocab_feedback", columnDefinition = "TEXT")
    private String vocabFeedback;

    @Column(name = "logic_score")
    private Integer logicScore;

    @Column(name = "logic_feedback", columnDefinition = "TEXT")
    private String logicFeedback;

    @Column(name = "fluency_score")
    private Integer fluencyScore;

    @Column(name = "fluency_feedback", columnDefinition = "TEXT")
    private String fluencyFeedback;

    @Column(name = "relevance_score")
    private Integer relevanceScore;

    @Column(name = "relevance_feedback", columnDefinition = "TEXT")
    private String relevance_feedback;

    /**
     * 오꿀쌤의 교정 스크립트 (AI가 다듬어준 문장)
     */
    @Column(name = "enhanced_script", columnDefinition = "TEXT")
    private String enhancedScript;

    /**
     * 분석 결과를 한 번에 업데이트하는 정적 팩토리 메서드 또는 생성자
     */
    public static TypeFeedback createFeedback(ExamAnswer examAnswer,  // ✅ ExamAnswer 추가
                                              Integer gScore, String gFb,
                                              Integer vScore, String vFb,
                                              Integer lScore, String lFb,
                                              Integer fScore, String fFb,
                                              String enhanced) {
        return TypeFeedback.builder()
                .answerId(examAnswer.getId())
                .examAnswer(examAnswer)
                .grammarScore(gScore)
                .grammarFeedback(gFb)
                .vocabScore(vScore)
                .vocabFeedback(vFb)
                .logicScore(lScore)
                .logicFeedback(lFb)
                .fluencyScore(fScore)
                .fluencyFeedback(fFb)
                .enhancedScript(enhanced)
                .build();
    }
}
