package site.okkul.be.domain.survey.mapper;

import org.springframework.stereotype.Component;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.dto.response.SelectedTopic;
import site.okkul.be.domain.survey.dto.response.SurveyResponse;
import site.okkul.be.domain.survey.dto.response.SurveySummaryResponse;
import site.okkul.be.domain.survey.entity.*;
import site.okkul.be.domain.topic.response.TopicCategory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SurveyMapper {

    /**
     * SurveyCreateRequest (Dto) -> Survey (Entity)
     * @param userId
     * @param request
     * @return Survey
     */
    public Survey toEntity(Long userId, SurveyCreateRequest request) {
        // 1. 사용자가 직접 선택한 토픽 ID들을 수집합니다.
        Set<Long> allTopicIds = Stream.of(
                        request.getLeisure(), request.getHobby(), request.getExercise(), request.getHoliday()
                )
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // 2. 응답에 따른 토픽 ID를 비즈니스 규칙에 따라 추가합니다.
        // 규칙 1: 직업 유무 및 관리직 여부
        if (request.getHasJob()) {
            if (request.getManager()) {
                allTopicIds.add(501L);  // hasJob && isManager
            } else { // isManager가 false 또는 null인 경우
                allTopicIds.add(502L);  // hasJob && !isManager
            }
        } else {
            allTopicIds.add(503L);      // !hasJob
        }

        // 규칙 2: 학생 여부
        if (request.getStudent()) {
            allTopicIds.add(601L);      // 학생 True
        } else {
            allTopicIds.add(602L);      // 학생 False
        }

        // 규칙 3: 거주 형태
        Integer residenceId = request.getResidenceAnswerId();
        if (residenceId != null) {
            switch (residenceId) {
                case 1 -> allTopicIds.add(701L);
                case 2 -> allTopicIds.add(702L);
                case 3 -> allTopicIds.add(703L);
                case 4 -> allTopicIds.add(704L);
                case 5 -> allTopicIds.add(705L);
            }
        }

        // 3. 최종적으로 수집된 모든 토픽 ID를 사용하여 엔티티를 생성합니다.
        return Survey.builder()
                .userId(userId)
                .occupation(OccupationType.fromValue(request.getOccupationAnswerId()))
                .teachAt(TeachingLevel.fromValue(request.getClassTypeAnswerId()))
                .hasJob(request.getHasJob() != null ? request.getHasJob() : false)
                .workPeriod(WorkPeriod.fromValue(request.getWorkPeriodAnswerId()))
                .isManager(request.getManager())
                .isStudent(request.getStudent())
                .classType(ClassType.fromValue(request.getClassTypeAnswerId()))
                .residence(ResidenceType.fromValue(request.getResidenceAnswerId()))
                .level(request.getLevel())
                .topicIds(allTopicIds)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Survey, TopicCategory (Entity) -> SurveyResponse (Dto)
     * @param survey
     * @param topicCategories
     * @return SurveyResponse
     */
    public SurveyResponse toResponseDto(Survey survey, List<TopicCategory> topicCategories) {
        List<SelectedTopic> selectedTopics = topicCategories.stream()
                .map(topicDto -> new SelectedTopic(topicDto.getTopicId(), topicDto.getTopicName()))
                .collect(Collectors.toList());

        return SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .createdAt(survey.getCreatedAt())
                .occupation(survey.getOccupation() != null ? survey.getOccupation().toString() : null)
                .workPeriod(survey.getWorkPeriod() != null ? survey.getWorkPeriod().toString() : null)
                .residence(survey.getResidence() != null ? survey.getResidence().toString() : null)
                .teachAt(survey.getTeachAt() != null ? survey.getTeachAt().toString() : null)
                .classType(survey.getResidence() != null ? survey.getResidence().toString() : null)
                .level(survey.getLevel())
                .hasJob(survey.getHasJob())
                .student(survey.getIsStudent())
                .manager(survey.getIsManager())
                .selectedTopics(selectedTopics)
                .build();

    }

    /**
     * Survey (Entity) -> SurveySummaryResponse (Dto)
     * @param survey
     * @return SurveySummaryResponse
     */
    public SurveySummaryResponse toSummaryDto(Survey survey) {
        return SurveySummaryResponse.builder()
                .surveyId(survey.getSurveyId())
                .createdAt(survey.getCreatedAt())
                .occupation(survey.getOccupation() != null ? survey.getOccupation().toString() : null)
                .workPeriod(survey.getWorkPeriod() != null ? survey.getWorkPeriod().toString() : null)
                .residence(survey.getResidence() != null ? survey.getResidence().toString() : null)
                .level(survey.getLevel())
                .hasJob(survey.getHasJob())
                .student(survey.getIsStudent())
                .manager(survey.getIsManager())
                .topicList(new ArrayList<>(survey.getTopicIds()))
                .build();
    }
}
