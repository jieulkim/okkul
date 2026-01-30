package site.okkul.be.domain.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개별 답변에 대한 AI의 상세 피드백 정보 엔티티
 */


@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerScore {

	// 0~100 사이의 점수
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

}
