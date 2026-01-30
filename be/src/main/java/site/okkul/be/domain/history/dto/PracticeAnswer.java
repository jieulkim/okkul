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
@Schema(description = "사용자 답변 정보 (스크립트 및 녹음)")
public class PracticeAnswer {

    @Schema(description = "한글 스크립트", example = "저는 여행을 좋아합니다.")
    private String koreanScript;

    @Schema(description = "영어 스크립트 (STT)", example = "I like travel.")
    private String englishScript;

    @Schema(description = "녹음 파일 URL", example = "https://s3.aws.../record.mp3")
    private String recordUrl;
}
