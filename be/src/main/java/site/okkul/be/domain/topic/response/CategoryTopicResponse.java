package site.okkul.be.domain.topic.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "설문조사용 카테고리별 토픽 목록 응답 DTO")
public class CategoryTopicResponse {
    @Schema(description = "카테고리 목록")
    private List<CategoryInfo> categories;

}