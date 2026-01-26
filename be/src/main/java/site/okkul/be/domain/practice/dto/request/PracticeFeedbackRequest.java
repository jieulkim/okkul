package site.okkul.be.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "유형별 연습 피드백 요청 DTO")
public class PracticeFeedbackRequest {

    @Schema(description = "사용자 한국어 스크립트", example = "안녕하세요, 제 이름은 OOO입니다.")
    private String koreanScript;

    @NotBlank
    @Schema(description = "사용자 영어 스크립트", example = "Hello, my name is OOO.")
    private String englishScript;
}
