package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@Schema(description = "유형별 연습 히스토리 상세 조회 응답 (메타데이터 + 전체 문답 사이클)")
public class PracticeHistoryDetailResponse {

    @Schema(description = "연습 세션 ID", example = "101")
    private Long practiceId;

    @Schema(description = "토픽 ID", example = "50")
    private Long topicId;

    @Schema(description = "선택한 토픽 이름", example = "영화보기")
    private String topicTitle;

    @Schema(description = "문제 유형 ID", example = "3")
    private Long typeId;

    @Schema(description = "문제 유형 코드", example = "COMBO3")
    private String typeName;

    @Schema(description = "연습 시작 일시", example = "2024-05-21T05:30:00Z")
    private Instant startedAt;

    @Schema(description = "질문별 상세 내역 리스트")
    private List<PracticeQuestionDetail> questions;
}