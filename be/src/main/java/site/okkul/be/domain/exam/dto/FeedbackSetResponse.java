package site.okkul.be.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "영역별 상세 피드백 정보")
public class FeedbackSetResponse {
    @Schema(description = "영역 점수 (1-5 또는 0-100)", example = "4")
    private Integer score;

    @Schema(description = "영역별 맞춤 피드백 내용", example = "과거 시제 활용이 매우 정확했습니다.")
    private String feedback;
}
