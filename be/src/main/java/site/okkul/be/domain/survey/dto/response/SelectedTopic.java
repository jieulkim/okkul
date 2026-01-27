package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "설문조사 생성 응답 DTO")
public class SelectedTopic {

    @Schema(description = "토픽 ID", example = "101")
    private final Long topicId;
    @Schema(description = "토픽 이름", example = "여행가기")
    private final String topicName;

}
