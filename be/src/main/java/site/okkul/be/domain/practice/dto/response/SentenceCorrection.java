package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "스크립트 교정 항목")
public class SentenceCorrection {

    @Schema(description = "타겟 문장", example = "I am usually go to the school in morning.")
    private String targetSentence;

    @Schema(description = "수정이 필요한 원본 문구", example = "I am go to the school.")
    private String originalSegment;

    @Schema(description = "교정된 문구", example = "I go to school.")
    private String correctedSegment;

    @Schema(description = "교정 이유 또는 설명", example = "불필요한 'am'이 사용되었습니다.")
    private String comment;

    @Schema(description = "문장 순서", example = "1")
    private Integer sentenceOrder;

}