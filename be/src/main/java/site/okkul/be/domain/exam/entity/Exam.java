package site.okkul.be.domain.exam.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.okkul.be.domain.qustion.entity.QuestionSet;

/**
 * 모의고사 응시 세션 엔티티
 * <p>
 * 역할
 * - 시험 상태(시작/종료/완료 여부) 관리
 * - 1~7 이후 난이도 재조정(adjustedDifficulty) 저장
 * - 시험 전체에서 사용할 Topic 순서(topicOrder)와 커서(topicCursor) 관리
 */
@Entity
@Table(name = "exam")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Exam {

	/**
	 * 모의고사 고유 ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "exam_id")
	private Long id;

	/**
	 * 종료 시간
	 * <p>Null 이면 시험 안끝남</p>
	 * <p>시간데이터가 있으면 종료이고, 그때 끝남</p>
	 */
	@Column
	private Instant endAt;

	/**
	 * 사용자가 처음 선택한 난이도 (1~6단계)
	 * - 1~7번 문항 출제 시 기준
	 */
	@Column(nullable = false, updatable = false)
	private Integer initialDifficulty;

	/**
	 * 7번 문항 이후 변경된 난이도
	 * - 8~마지막 문항 출제 시 기준
	 */
	@Column
	private Integer adjustedDifficulty;

	// 단방향 매핑
	@ManyToMany
	@JoinTable(name = "exam_question_set", joinColumns = @JoinColumn(name = "exam_id"), inverseJoinColumns = @JoinColumn(name = "question_set_id"))
	@OrderColumn(name = "question_order")
	private List<QuestionSet> questionSets;

	// 양방향 매핑
	@JsonManagedReference
	@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id.questionOrder ASC")
	private List<ExamAnswer> examAnswers;

	/**
	 * 참조하는 설문조사 ID
	 */
	@Column(name = "survey_id", nullable = false)
	private Long surveyId;

	/**
	 * 응시 사용자 ID
	 */
	@Column(name = "user_id", nullable = false)
	private Long userId;

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

	/**
	 * 모의고사 팩토리메소드
	 *
	 * @param initialDifficulty 초기 난이도
	 * @param surveyId          참조하는 설문조사 ID
	 * @param userId            응시 사용자 ID
	 * @return 생성된 모의고사 엔티티
	 */
	public static Exam create(Long surveyId, Integer initialDifficulty, Long userId) {
		return Exam.builder()
				.id(null)
				.initialDifficulty(initialDifficulty)
				.endAt(null)
				.adjustedDifficulty(null)
				.questionSets(new ArrayList<>())
				.examAnswers(new ArrayList<>())
				.surveyId(surveyId)
				.userId(userId)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
	}

	/**
	 * 시험 종료 처리
	 * - completed=true 로 바꾸고
	 * - endAt에 종료 시각을 기록
	 */
	public void completeExam() {
		this.endAt = Instant.now();
	}

	/**
	 * 난이도 재조정 반영 (7번 이후)
	 * - 난이도는 위 아래로 1칸만 조정이 가능합니다
	 *
	 * @throws IllegalArgumentException 잘못된 난이도를 넣었을 경우
	 */
	public void updateAdjustedDifficulty(Integer newDifficulty) {
		if (Math.abs(newDifficulty - this.initialDifficulty) < 2) {
			this.adjustedDifficulty = newDifficulty;
			this.updatedAt = Instant.now();
		} else {
			throw new IllegalArgumentException("잘못된 값을 넣었다");
		}
	}
}
