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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
	@OneToMany
	@JoinTable(
			name = "exam_question_set",
			joinColumns = @JoinColumn(name = "exam_id"),
			inverseJoinColumns = @JoinColumn(name = "question_set_id")
	)
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
	 * 시험 종료 처리
	 * - completed=true 로 바꾸고
	 * - endAt에 종료 시각을 기록
	 */
	public void completeExam() {
//		this.completed = true;
		this.endAt = Instant.now();
	}

	/**
	 * 난이도 재조정 반영 (7번 이후)
	 * - 값이 1~6 범위를 벗어나면 반영하지 않음
	 */
	public void updateAdjustedDifficulty(Integer newDifficulty) {
		if (newDifficulty != null && newDifficulty >= 1 && newDifficulty <= 6) {
			this.adjustedDifficulty = newDifficulty;
		}
	}

	/**
	 * 시험 시작 시 호출: topicOrder와 topicCursor를 초기화
	 *
	 * @param topicIds 설문에서 선택된 topicId 목록(셔플된 상태 권장)
	 */
	public void initializeTopicOrder(List<Long> topicIds) {
		if (topicIds == null || topicIds.isEmpty()) {
			throw new IllegalArgumentException("topicIds가 비어있습니다.");
		}
//		this.topicOrder = topicIds.stream()
//				.map(String::valueOf)
//				.collect(Collectors.joining(","));
//		this.topicCursor = 0;
	}

	/**
	 * 현재 커서가 가리키는 topicId를 반환
	 * <p>
	 * 동작
	 * - topicOrder 문자열을 파싱하여 topic 리스트를 만든 뒤
	 * - topicCursor가 범위를 넘으면 0으로 순환시켜
	 * - 현재 topicId를 반환
	 */
	public Long getCurrentTopicId() {
//		if (topicOrder == null || topicOrder.isBlank()) {
//			throw new IllegalStateException("topicOrder가 설정되지 않았습니다.");
//		}
//		if (topicCursor == null) {
//			topicCursor = 0;
//		}
//
//		List<Long> topics = Arrays.stream(topicOrder.split(","))
//				.map(String::trim)
//				.filter(s -> !s.isEmpty())
//				.map(Long::valueOf)
//				.toList();
//
//		if (topics.isEmpty()) {
//			throw new IllegalStateException("topicOrder 파싱 결과가 비어있습니다.");
//		}
//
//		if (topicCursor >= topics.size()) {
//			topicCursor = 0; // 주제 순환
//		}
//
//		return topics.get(topicCursor);
		return 1L;
	}

	/**
	 * 문제 세트 하나 출제 후 호출: 다음 주제를 사용하도록 커서를 1 증가
	 */
	public void incrementTopicCursor() {
//		if (topicCursor == null) {
//			topicCursor = 0;
//		}
//		topicCursor++;
	}
}
