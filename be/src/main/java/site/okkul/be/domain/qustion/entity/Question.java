package site.okkul.be.domain.qustion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "question_bank")
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_id")
	private Long id;

	@Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
	private String questionText;

	@Column(name = "audio_url", columnDefinition = "TEXT", nullable = false)
	private String audioUrl;

	@Column(name = "order_index") // order는 SQL 예약어인 경우가 많아 필드명 조정 권장
	private Integer order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "set_id", nullable = false)
	private QuestionSet questionSet;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public void update(String questionText, String audioUrl, Integer order) {
		this.questionText = questionText;
		this.audioUrl = audioUrl;
		this.order = order;
		this.updatedAt = Instant.now();
	}

	public void assignTo(QuestionSet questionSet) {
		this.questionSet = questionSet;
	}
}
