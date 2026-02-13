package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유형별 연습 히스토리 목록 조회용 요약 정보")
public class PracticeHistorySummary {

    @Schema(description = "연습 ID (PK)", example = "101")
    private Long practiceId;

    @Schema(description = "연습 시작 일시", example = "2024-05-21T14:30:00")
    private Instant startedAt;

    @Schema(description = "선택한 토픽 ID", example = "50")
    private Long topicId;

    @Schema(description = "선택한 토픽 이름")
    private String topic;

    @Schema(description = "문제 유형 ID", example = "3")
    private Long typeId;

    @Schema(description = "문제 유형")
    private String typeName;
}
