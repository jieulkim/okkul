package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "설문조사용 카테고리별 소재 목록 응답 DTO")
public class SurveyCategoryTopicResponse {

    @Schema(description = "전체 카테고리 개수")
    private Integer count;

    @Schema(description = "카테고리 목록")
    private List<CategoryInfo> categories;

}