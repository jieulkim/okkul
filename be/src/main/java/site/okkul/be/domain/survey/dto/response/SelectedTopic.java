package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "선택된 개별 소재(토픽) 정보")
public class SelectedTopic {
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    @Schema(description = "토픽 ID", example = "101")
    private Long topicId;
    private String topicName;
}
