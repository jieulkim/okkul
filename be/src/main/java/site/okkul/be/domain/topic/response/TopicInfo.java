package site.okkul.be.domain.topic.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.okkul.be.domain.topic.entity.Topic;

@Getter
@AllArgsConstructor
@Schema(description = "토픽 정보")
public class TopicInfo {
    @Schema(description = "토픽 ID", example = "101")
    private Long id;
    @Schema(description = "토픽 이름", example = "여행가기")
    private String name;

    public static TopicInfo from(Topic topic) {
        return new TopicInfo(topic.getId(), topic.getTopicName());
    }
}