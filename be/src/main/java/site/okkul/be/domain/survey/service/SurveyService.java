package site.okkul.be.domain.survey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.dto.response.*;
import site.okkul.be.domain.survey.entity.SurveyErrorCode;
import site.okkul.be.domain.survey.mapper.SurveyMapper;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.topic.response.SelectedTopics;
import site.okkul.be.domain.topic.response.TopicCategory;
import site.okkul.be.domain.topic.service.TopicService;
import site.okkul.be.global.exception.BusinessException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyJpaRepository surveyJpaRepository;
    private final TopicService topicService;
    private final SurveyMapper surveyMapper;

    @Transactional
    public SurveyCreateResponse create(SurveyCreateRequest request, Long userId) {
        // 설문조사 생성 요청에 포함된 Topic ID 유효성 검사
        Set<Long> allTopicIds = new HashSet<>();
        allTopicIds.addAll(request.getLeisure());
        allTopicIds.addAll(request.getHobby());
        allTopicIds.addAll(request.getExercise());
        allTopicIds.addAll(request.getHoliday());
        List<TopicCategory> validTopicCategories = topicService.getTopicList(new ArrayList<>(allTopicIds));
        if (validTopicCategories.size() != allTopicIds.size()) {
            throw new BusinessException(SurveyErrorCode.INVALID_TOPIC_ID);
        }
        // DTO를 엔티티로 변환 후 저장
        Survey survey = surveyMapper.toEntity(userId, request);
        Survey savedSurvey = surveyJpaRepository.save(survey);
        return new SurveyCreateResponse(savedSurvey.getSurveyId());
    }

    @Transactional(readOnly = true)
    public SurveyListResponse findAllByUser(Long userId) {
        List<Survey> surveys = surveyJpaRepository.findAllByUserId(userId);
        List<SurveySummaryResponse> summaryResponses = surveys.stream()
                .map(surveyMapper::toSummaryDto)
                .collect(Collectors.toList());
        return new SurveyListResponse(summaryResponses);
    }

    @Transactional(readOnly = true)
    public SurveyResponse findById(Long surveyId, Long userId) {
        Survey survey = surveyJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(SurveyErrorCode.SURVEY_NOT_FOUND));
        List<TopicCategory> topicCategories = topicService.getTopicList(new ArrayList<>(survey.getTopicIds()));
        return surveyMapper.toResponseDto(survey, topicCategories);
    }

    @Transactional(readOnly = true)
    public SelectedTopics findTopicsBySurveyId(Long surveyId, Long userId) {
        Survey survey = surveyJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(SurveyErrorCode.SURVEY_NOT_FOUND));
        List<Long> topicIds = new ArrayList<>(survey.getTopicIds());
        List<TopicCategory> topicCategories = topicService.getTopicList(topicIds);
        List<SelectedTopic> selectedTopics = topicCategories.stream()
                .map(dto -> new SelectedTopic(dto.getTopicId(), dto.getTopicName()))
                .collect(Collectors.toList());
        return new SelectedTopics(selectedTopics);
    }
}
