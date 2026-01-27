package site.okkul.be.domain.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.topic.response.TopicCategory;
import site.okkul.be.domain.topic.response.CategoryInfo;
import site.okkul.be.domain.topic.response.CategoryTopicResponse;
import site.okkul.be.domain.topic.response.TopicInfo;
import site.okkul.be.domain.topic.repository.TopicJpaRepository;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TopicService {

    private final TopicJpaRepository topicJpaRepository;


    @Transactional(readOnly = true)
    public CategoryTopicResponse getSurveyTopics() {
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L, 4L);

        // 해당되는 카테고리의 모든 토픽 데이터 조회
        List<TopicCategory> allTopicCategories = topicJpaRepository.findAllByCategoryIdIn(categoryIds).stream()
                .map(TopicCategory::from)
                .toList();

        // 카테고리 id를 통해 TopicInfo를 생성해 그룹핑
        Map<Long, List<TopicInfo>> topicsByCategoryId = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        for (TopicCategory dto : allTopicCategories) {
            topicsByCategoryId.computeIfAbsent(dto.getCategoryId(), k -> new ArrayList<>())
                .add(new TopicInfo(dto.getTopicId(), dto.getTopicName()));
            categoryNames.putIfAbsent(dto.getCategoryId(), dto.getCategoryName());
        }
        // 최종 DTO 리스트
        List<CategoryInfo> categoryInfos = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            String categoryName = categoryNames.get(categoryId);
            List<TopicInfo> topicInfos = topicsByCategoryId.getOrDefault(categoryId, Collections.emptyList());
            if (categoryName != null) {
                categoryInfos.add(new CategoryInfo(categoryId, categoryName, topicInfos));
            }
        }
        return new CategoryTopicResponse(categoryInfos);
    }

    @Transactional(readOnly = true)
    public List<TopicCategory> getTopicList(List<Long> topicIds) {
        return topicJpaRepository.findAllById(topicIds).stream()
                .map(TopicCategory::from)
                .toList();
    }

}
