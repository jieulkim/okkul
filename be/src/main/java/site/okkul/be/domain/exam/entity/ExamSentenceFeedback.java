package site.okkul.be.domain.exam.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_sentence_feedback")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExamSentenceFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_id")
	private Long id;

	private String targetSentence;

	private String targetSegment;

	private String correctedSegment;

	private String comment;

	private Integer sentenceOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonBackReference
	private ExamAnswer examAnswer;

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
}
