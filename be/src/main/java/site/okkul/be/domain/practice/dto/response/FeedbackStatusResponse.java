package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import site.okkul.be.domain.practice.entity.FeedbackStatus;

@Getter
@Schema(description = "피드백 상태 정보 응답")
public class FeedbackStatusResponse {
    @Schema(description = "피드백 처리 상태", example = "PROCESSING")
    private final FeedbackStatus status;

    public FeedbackStatusResponse(FeedbackStatus status) {
        this.status = status;
    }
}
