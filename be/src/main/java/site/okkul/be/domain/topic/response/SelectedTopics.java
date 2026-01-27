package site.okkul.be.domain.topic.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.okkul.be.domain.survey.dto.response.SelectedTopic;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(name = "선택된 토픽 목록")
public class SelectedTopics {
    private List<SelectedTopic> selectedTopics;
}
