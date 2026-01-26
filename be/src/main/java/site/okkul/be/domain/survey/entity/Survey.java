package site.okkul.be.domain.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.user.entity.User;

/**
 * 설문조사 엔티티
 *
 * @author 김남주
 */
@Entity
@Getter
@NoArgsConstructor
public class Survey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyId;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	private Integer level; // 1~6

	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "survey_topics",
			joinColumns = @JoinColumn(name = "survey_id"),
			inverseJoinColumns = @JoinColumn(name = "topic_id")
	)
	private Set<Topic> topics;

	/**
	 * 생성 일시
	 */
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
}
