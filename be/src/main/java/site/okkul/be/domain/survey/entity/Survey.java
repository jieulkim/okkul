package site.okkul.be.domain.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OccupationType occupation;

	private Boolean hasJob;

	@Enumerated(EnumType.STRING)
	private WorkPeriod workPeriod;

	@Enumerated(EnumType.STRING)
	private TeachingLevel teachAt;

	private Boolean isManager;
	private Boolean isStudent;

	@Enumerated(EnumType.STRING)
	private ClassType classType;

	private Integer level; // 1~6

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResidenceType residence;

	// TODO ... 나머지 필드 및 연관관계


	/**
	 * 생성 일시
	 */
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
}
