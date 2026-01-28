package site.okkul.be.domain.exam.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	@EmbeddedId
	private ExamAnswerId id;

	@MapsId("examId") // 내부 클래스의 필드명과 매핑
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	@JsonBackReference
	private Exam exam;

	@Column(name = "question_id")
	private Long questionId;

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
	 * 답변 피드백 모음
	 */
	@Embedded
	ExamAnswerFeedback answerFeedbacks;

	/**
	 * 답변 점수 모음
	 */
	@Embedded
	private ExamAnswerScore answerScores;

	@OneToMany(mappedBy = "examAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<ExamSentenceFeedback> sentenceFeedbacks;

	/**
	 * 응시 시작 시간
	 */
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/**
	 * 마지막 업데이트 시간
	 */
	@Column(nullable = false)
	private Instant updatedAt;

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

	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@Getter // ID 객체에서 값을 꺼낼 때 필요
	public static class ExamAnswerId implements Serializable {
		@Column(name = "exam_id")
		private Long examId; // @MapsId에 의해 매핑됨

		@Column(name = "question_order")
		private Integer questionOrder;
	}
}
