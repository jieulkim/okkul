package site.okkul.be.domain.topic.response;

import lombok.Getter;
import site.okkul.be.domain.topic.entity.Topic;

@Getter
public class TopicCategory {
    private final Long topicId;
    private final String topicName;
    private final Long categoryId;
    private final String categoryName;

    private TopicCategory(Long topicId, String topicName, Long categoryId, String categoryName) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public static TopicCategory from(Topic topic) {
        if (topic == null) {
            return null;
        }
        Long catId = topic.getCategory() != null ? topic.getCategory().getId() : null;
        String catName = topic.getCategory() != null ? topic.getCategory().getCategoryName() : null;

        return new TopicCategory(
                topic.getId(),
                topic.getTopicName(),
                catId,
                catName
        );
    }
}
