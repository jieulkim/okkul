package site.okkul.be.domain.topic.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "카테고리 정보")
public class CategoryInfo {
    @Schema(name = "카테고리 ID", example = "1")
    private Long id;
    @Schema(name = "카테고리 이름", example = "학생여부")
    private String name;
    @Schema(name = "카테고리에 속한 토픽 목록")
    private List<TopicInfo> topics;

    public static CategoryInfo of(Long categoryId, String categoryName, List<TopicInfo> topicInfos) {
        return CategoryInfo.builder()
                .id(categoryId)
                .name(categoryName)
                .topics(topicInfos).build();
    }
}
