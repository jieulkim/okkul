package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "생성된 유형별 연습 답변 ID 응답")
public class PracticeAnswerIdResponse {
    @Schema(description = "생성된 유형별 연습 답변 ID", example = "1")
    private final Long practiceAnswerId;

    public PracticeAnswerIdResponse(Long practiceAnswerId) {
        this.practiceAnswerId = practiceAnswerId;
    }
}
