package site.okkul.be.domain.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerFeedback {
	@Column(name = "logic_feedback", columnDefinition = "TEXT")
	private String logicFeedback;

	@Column(name = "fluency_feedback", columnDefinition = "TEXT")
	private String fluencyFeedback;

	@Column(name = "relevance_feedback", columnDefinition = "TEXT")
	private String relevance_feedback;
}
