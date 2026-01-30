package site.okkul.be.domain.practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "practice_sentence_feedback")
public class PracticeSentenceFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "target_sentence", columnDefinition = "TEXT")
    private String targetSentence;

    @Column(name = "target_segment", columnDefinition = "TEXT")
    private String targetSegment;

    @Column(name = "improved_segment", columnDefinition = "TEXT")
    private String improvedSegment;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "sentence_order")
    private Integer sentenceOrder;

    @Column(name = "sentence_score")
    private Integer sentenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_answer_id")
    private PracticeAnswer practiceAnswer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void setPracticeAnswer(PracticeAnswer practiceAnswer) {
        this.practiceAnswer = practiceAnswer;
    }
}