package site.okkul.be.domain.qustion.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.okkul.be.domain.survey.entity.ClassType;
import site.okkul.be.domain.survey.entity.OccupationType;
import site.okkul.be.domain.survey.entity.ResidenceType;
import site.okkul.be.domain.survey.entity.TeachingLevel;
import site.okkul.be.domain.survey.entity.WorkPeriod;
import site.okkul.be.domain.topic.entity.Topic;

@Entity
@Table(name = "question_set")
@Getter
@NoArgsConstructor
public class QuestionSet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "set_id")
	private Long id;

	@Column(name = "level", nullable = false)
	private Integer level; // 1~6

	@Column(name = "question_cnt")
	private Integer questionCnt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "type_id", nullable = false)
	private QuestionType questionType;

	@OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL)
	@OrderBy("order ASC")
	private List<Question> questions = new ArrayList<>();

	// 타겟 조건들
	@Enumerated(EnumType.STRING)
	@Column(name = "target_occupation")
	private OccupationType targetOccupation; // 특정 직업군 타겟 (null이면 공통)

	@Column(name = "is_manager_only")
	private Boolean isManagerOnly; // 관리직 전용 여부

	@Column(name = "is_student_only")
	private Boolean isStudentOnly; // 학생 전용 여부

	@Column(name = "class_type")
	@Enumerated(EnumType.STRING)
	private ClassType classType; // 특정 수업 형태 타겟 (null이면 공통)

	@Enumerated(EnumType.STRING)
	@Column(name = "target_residence")
	private ResidenceType targetResidence; // 특정 거주 형태 타겟

	@Enumerated(EnumType.STRING)
	@Column(name = "target_work_period")
	private WorkPeriod targetWorkPeriod;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_teaching_level")
	private TeachingLevel targetTeachingLevel;

	// 생성 및 수정 시점
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
