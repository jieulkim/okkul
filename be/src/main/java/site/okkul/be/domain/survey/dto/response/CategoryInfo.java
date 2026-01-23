package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "소재 카테고리 정보")
public class CategoryInfo {
    @Schema(description = "카테고리 ID")
    private Long categoryId;
    @Schema(description = "카테고리 이름")
    private String categoryName;
    @Schema(description = "해당 카테고리에 속한 소재(토픽) 목록")
    private List<TopicInfo> items;
}