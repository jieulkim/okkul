package site.okkul.be.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import site.okkul.be.domain.qustion.entity.Question;

/**
 * 개별 문항에 대한 사용자의 답변 및 AI 분석 결과 엔티티
 */
@Entity
@Table(name = "exam_answer")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 답변 순서 (1~15번)
     */
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    /**
     * S3에 저장된 사용자 음성 파일 URL
     */
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    /**
     * AI가 음성을 텍스트로 변환한 결과 (STT)
     */
    @Column(name = "stt_script", columnDefinition = "TEXT")
    private String sttScript;

    /**
     * 답변 상태
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AnswerStatus status = AnswerStatus.READY;

    /**
     * 1. 음성 파일 업로드 완료 (READY -> UPLOADED)
     */
    public void uploadVoiceFile(String audioUrl) {
        this.audioUrl = audioUrl;
        this.status = AnswerStatus.UPLOADED;
    }

    /**
     * 2. STT 변환 시작 (UPLOADED -> STT_ONGOING)
     */
    public void startStt() {
        this.status = AnswerStatus.STT_ONGOING;
    }

    /**
     * 3. STT 완료 및 AI 분석 진입 (STT_ONGOING -> ANALYZING)
     */
    public void completeSttAndStartAnalysis(String sttScript) {
        this.sttScript = sttScript;
        this.status = AnswerStatus.ANALYZING;
    }

    /**
     * 4. AI 피드백 저장 완료 (ANALYZING -> COMPLETED)
     */
    public void finalizeAnalysis() {
        this.status = AnswerStatus.COMPLETED;
    }

    /**
     * 5. 오류 발생 시 (어느 단계에서든 호출)
     */
    public void markAsFailed() {
        this.status = AnswerStatus.FAILED;
    }
}
