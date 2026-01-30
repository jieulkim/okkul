package site.okkul.be.domain.practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "practice_answers")
public class PracticeAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_answer_id")
    private Long practiceAnswerId;

    @Column(name = "korean_script", columnDefinition = "TEXT")
    private String koreanScript;

    @Column(name = "english_script", columnDefinition = "TEXT")
    private String englishScript;

    @Column(name = "english_record_url", columnDefinition = "TEXT")
    private String englishRecordUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private QuestionSet questionSet;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "improved_answer", columnDefinition = "TEXT")
    private String improvedAnswer;

    @Column(name = "relevance_feedback", columnDefinition = "TEXT")
    private String relevanceFeedback;

    @Column(name = "logic_feedback", columnDefinition = "TEXT")
    private String logicFeedback;

    @Column(name = "fluency_feedback", columnDefinition = "TEXT")
    private String fluencyFeedback;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus feedbackStatus;

    @OneToMany(mappedBy = "practiceAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PracticeSentenceFeedback> feedbacks = new ArrayList<>();

    // --- 편의 메소드 ---

    public void updateStatus(FeedbackStatus status) {
        this.feedbackStatus = status;
    }

    public void applyFeedback(String improvedAnswer, String relevanceFeedback, String logicFeedback, String fluencyFeedback, List<PracticeSentenceFeedback> newFeedbacks) {
        this.improvedAnswer = improvedAnswer;
        this.relevanceFeedback = relevanceFeedback;
        this.logicFeedback = logicFeedback;
        this.fluencyFeedback = fluencyFeedback;

        this.feedbacks.clear();
        if (newFeedbacks != null) {
            this.feedbacks.addAll(newFeedbacks);
            // The bidirectional relationship is now set in the mapper
        }

        updateStatus(FeedbackStatus.COMPLETED);
    }
}