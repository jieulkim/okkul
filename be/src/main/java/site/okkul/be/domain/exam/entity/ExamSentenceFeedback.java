package site.okkul.be.domain.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSentenceFeedback {

	/**
	 * 대상 문장
	 */
	private String targetSentence;

	/**
	 * 대상 위치 (숙어 단어등)
	 */
	private String targetSegment;

	/**
	 * 개선된 문장
	 */
	private String correctedSegment;

	/**
	 * 코멘트
	 */
	private String comment;

	/**
	 * 문장 순서
	 * 1번 문장에 여러개의 피드백이 올 수 있는데, 하나의 문장을 선택하는 기능
	 */
	@Column(name = "sentence_order")
	private Integer sentenceOrder;

	// 생성 및 수정 시점
	@CreationTimestamp
	@JdbcTypeCode(SqlTypes.TIMESTAMP)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
}
