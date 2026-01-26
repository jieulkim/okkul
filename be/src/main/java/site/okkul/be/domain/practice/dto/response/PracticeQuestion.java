package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유형별 연습 모드 질문 상세 정보")
public class PracticeQuestion {

    @Schema(description = "문제 ID", example = "1201")
    private Long questionId;

    @Schema(description = "질문 순서", example = "1")
    private Integer questionOrder;

    @Schema(description = "질문 텍스트 (화면 표시용)", example = "Let's start the interview. Tell me about yourself.")
    private String questionText;

    @Schema(description = "질문 음성 파일 경로 (S3 등 스토리지 URL)", example = "https://cdn.okkul.site/audio/q1.mp3")
    private String audioUrl;
}
