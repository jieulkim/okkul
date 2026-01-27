package site.okkul.be.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 모의고사 응시 세션 엔티티
 *
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
     * 응시 시작 시간
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 종료 시간
     */
    private Instant endAt;

    /**
     * 시험 종료 여부
     */
    @Column(nullable = false)
    private boolean completed;

    /**
     * 사용자가 처음 선택한 난이도 (1~6단계)
     * - 1~7번 문항 출제 시 기준
     */
    @Column(nullable = false)
    private Integer initialDifficulty;

    /**
     * 7번 문항 이후 변경된 난이도
     * - 8~마지막 문항 출제 시 기준
     */
    private Integer adjustedDifficulty;

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
     * 시험 전체에서 사용할 주제 순서 (셔플 결과)
     * 예: "101,208,317,405"
     *
     * - 시험 시작 시 1회만 세팅
     * - 난이도 조정과 무관하게 유지되어 "주제 다양성"을 보장
     */
    @Column(name = "topic_order", length = 1000)
    private String topicOrder;

    /**
     * 현재까지 사용한 topic 인덱스
     * - QuestionSet 하나를 뽑을 때마다 1씩 증가
     */
    @Column(name = "topic_cursor")
    private Integer topicCursor;

    /**
     * 시험 종료 처리
     * - completed=true 로 바꾸고
     * - endAt에 종료 시각을 기록
     */
    public void completeExam() {
        this.completed = true;
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
        this.topicOrder = topicIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        this.topicCursor = 0;
    }

    /**
     * 현재 커서가 가리키는 topicId를 반환
     *
     * 동작
     * - topicOrder 문자열을 파싱하여 topic 리스트를 만든 뒤
     * - topicCursor가 범위를 넘으면 0으로 순환시켜
     * - 현재 topicId를 반환
     */
    public Long getCurrentTopicId() {
        if (topicOrder == null || topicOrder.isBlank()) {
            throw new IllegalStateException("topicOrder가 설정되지 않았습니다.");
        }
        if (topicCursor == null) {
            topicCursor = 0;
        }

        List<Long> topics = Arrays.stream(topicOrder.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();

        if (topics.isEmpty()) {
            throw new IllegalStateException("topicOrder 파싱 결과가 비어있습니다.");
        }

        if (topicCursor >= topics.size()) {
            topicCursor = 0; // 주제 순환
        }

        return topics.get(topicCursor);
    }

    /**
     * 문제 세트 하나 출제 후 호출: 다음 주제를 사용하도록 커서를 1 증가
     */
    public void incrementTopicCursor() {
        if (topicCursor == null) {
            topicCursor = 0;
        }
        topicCursor++;
    }
}
