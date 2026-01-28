package site.okkul.be.domain.topic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.topic.docs.TopicControllerDocs;
import site.okkul.be.domain.topic.response.CategoryTopicResponse;
import site.okkul.be.domain.topic.service.TopicService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/topics")
public class TopicController implements TopicControllerDocs {

    private final TopicService topicService;

    @Override
    @GetMapping
    public ResponseEntity<CategoryTopicResponse> getSurveyTopics() {
        CategoryTopicResponse response = topicService.getSurveyTopics();
        return ResponseEntity.ok(response);
    }
}
