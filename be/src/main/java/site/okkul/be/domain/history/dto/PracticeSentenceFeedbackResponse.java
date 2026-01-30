package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSentenceFeedbackResponse {

    @Schema(description = "타겟 문장 (원본 전체)", example = "I enjoy to travel.")
    private String targetSentence;

    @Schema(description = "대상 텍스트", example = "to travel")
    private String targetSegment;

    @Schema(description = "개선 텍스트", example = "traveling")
    private String correctedSegment;

    @Schema(description = "피드백 텍스트", example = "동명사를 사용하는 것이 더 자연스럽습니다.")
    private String comment;
}
