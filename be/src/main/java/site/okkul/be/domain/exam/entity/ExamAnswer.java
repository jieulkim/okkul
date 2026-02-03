package site.okkul.be.domain.exam.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OrderBy;
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

	/**
	 * S3에 저장된 사용자 음성 파일 URL
	 */
	@Column(name = "audio_url", length = 500)
	private String audioUrl;

	/**
	 * 유저가 생성한 텍스트
	 */
	@Column(name = "stt_script", columnDefinition = "TEXT")
	private String userAnswer;

	/**
	 * AI가 개선한 텍스트
	 */
	@Column(name = "improved_answer", columnDefinition = "TEXT")
	private String improvedAnswer;

	/**
	 * 답변 상태
	 */
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private AnswerStatus status = AnswerStatus.READY;

	// 답변 피드백 모음
	/**
	 * 논리성 피드백
	 */
	@Column(name = "logic_feedback", columnDefinition = "TEXT")
	private String logicFeedback;

	/**
	 * 발음 피드백
	 */
	@Column(name = "fluency_feedback", columnDefinition = "TEXT")
	private String fluencyFeedback;

	/**
	 * 주제 적합성 피드백
	 */
	@Column(name = "relevance_feedback", columnDefinition = "TEXT")
	private String relevanceFeedback;

	/**
	 * 답변 점수 모음
	 * 0~100점 사이의 값을 가짐
	 */
	@Column(name = "grammar_score")
	private Integer grammarScore;

	@Column(name = "vocab_score")
	private Integer vocabScore;

	@Column(name = "logic_score")
	private Integer logicScore;

	@Column(name = "fluency_score")
	private Integer fluencyScore;

	@Column(name = "relevance_score")
	private Integer relevanceScore;

	@CollectionTable(
			name = "exam_sentence_feedback",
			joinColumns = {
					@JoinColumn(name = "exam_answer_exam_id", referencedColumnName = "exam_id"),
					@JoinColumn(name = "exam_answer_question_order", referencedColumnName = "question_order")
			}
	)
	@ElementCollection
	@OrderBy("sentenceOrder ASC")
	private List<ExamSentenceFeedback> sentenceFeedbacks;

	/**
	 * 생성시간
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

	public void updateFromAi(
			Integer grammarScore,
			Integer vocabScore,
			Integer logicScore,
			Integer fluencyScore,
			Integer relevanceScore,
			String improvedAnswer,
			String logicFeedback,
			String fluencyFeedback,
			String relevanceFeedback,
			List<ExamSentenceFeedback> sentenceFeedbacks
	) {
		this.grammarScore = grammarScore;
		this.vocabScore = vocabScore;
		this.logicScore = logicScore;
		this.fluencyScore = fluencyScore;
		this.relevanceScore = relevanceScore;
		this.improvedAnswer = improvedAnswer;
		this.logicFeedback = logicFeedback;
		this.fluencyFeedback = fluencyFeedback;
		this.relevanceFeedback = relevanceFeedback;
		this.sentenceFeedbacks = sentenceFeedbacks;
	}
}
