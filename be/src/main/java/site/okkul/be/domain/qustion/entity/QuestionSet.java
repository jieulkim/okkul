package site.okkul.be.domain.qustion.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import site.okkul.be.domain.topic.entity.Topic;

@Entity
@Table(name = "question_set")
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
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

//	@ManyToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "type_id", nullable = false)
	@Enumerated(EnumType.STRING)
	private QuestionType questionType;

	@Builder.Default
	@OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL)
	@OrderBy("order ASC")
	private List<Question> questions = new ArrayList<>();

	// 생성 및 수정 시점
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public void update(Integer level, Topic topic, QuestionType questionType) {
		this.level = level;
		this.questionCnt = questions.size();
		this.topic = topic;
		this.questionType = questionType;
		this.updatedAt = Instant.now();
	}
}
