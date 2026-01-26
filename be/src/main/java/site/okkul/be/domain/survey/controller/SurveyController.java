package site.okkul.be.domain.survey.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.okkul.be.domain.survey.docs.SurveyControllerDocs;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.dto.response.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * SurveyController의 임시 구현체
 * 실제 데이터베이스 연동 없이 DTO에 담긴 더미 데이터를 반환
 */
@Slf4j
@RestController
@RequestMapping("/surveys")
public class SurveyController implements SurveyControllerDocs {

    @Override
    @GetMapping("/topics")
    public ResponseEntity<SurveyCategoryTopicResponse> getSurveyTopics() {
        log.info("Request received for getSurveyTopics API");
        List<CategoryInfo> categories = Arrays.asList(
                new CategoryInfo(1L, "여가 활동", Arrays.asList(
                        new TopicInfo(101L, "영화보기"),
                        new TopicInfo(102L, "클럽/나이트클럽 가기"),
                        new TopicInfo(103L, "공연보기"),
                        new TopicInfo(104L, "콘서트보기")
                )),
                new CategoryInfo(2L, "취미나 관심사", Arrays.asList(
                        new TopicInfo(201L, "음악 감상하기"),
                        new TopicInfo(202L, "악기 연주하기")
                )),
                new CategoryInfo(3L, "운동", Arrays.asList(
                        new TopicInfo(301L, "걷기"),
                        new TopicInfo(302L, "조깅")
                )),
                new CategoryInfo(4L, "휴가나 출장", Arrays.asList(
                        new TopicInfo(401L, "집에서 보내는 휴가"),
                        new TopicInfo(402L, "국내여행"),
                        new TopicInfo(403L, "해외여행")
                ))
        );

        SurveyCategoryTopicResponse response = new SurveyCategoryTopicResponse(categories.size(), categories);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<SurveyCreateResponse> createSurvey(@RequestBody SurveyCreateRequest surveyCreateRequest) {
        // TODO: 추후 Spring Security 도입 시 @AuthenticationPrincipal 어노테이션으로 userId를 가져와야 합니다.
        long userId = 1L;
        log.info("Request received for createSurvey API for user: {}", userId);

        SurveyCreateResponse response = new SurveyCreateResponse(1L); // 더미 설문조사 ID
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<SurveyListResponse> getSurveyList() {
        // TODO: 추후 Spring Security 도입 시 @AuthenticationPrincipal 어노테이션으로 userId를 가져와야 합니다.
        long userId = 1L;
        log.info("Request received for getSurveyList API for user: {}", userId);

        List<Long> topicList = Arrays.asList(101L, 102L, 103L, 201L, 202L);

        List<SurveySummaryResponse> surveyList = Arrays.asList(
                new SurveySummaryResponse(1L, Instant.now().minus(5, ChronoUnit.DAYS), "COMPANY", 3, true, false, "LESS2M", true, "LIVE_ALONE", topicList),
                new SurveySummaryResponse(2L, Instant.now().minus(2, ChronoUnit.DAYS), "STUDENT", 5, false, true, null, false, "FAMILY", topicList),
                new SurveySummaryResponse(3L, Instant.now(), "FREE", 4, false, false, "MANY", true, "SCHOOL", topicList)
        );

        return ResponseEntity.ok(new SurveyListResponse(surveyList));
    }

    @Override
    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> getSurveyById(@PathVariable("surveyId") Long surveyId) {
        // TODO: 추후 Spring Security 도입 시 @AuthenticationPrincipal 어노테이션으로 userId를 가져와야 합니다.
        long currentUserId = 1L;
        log.info("Request received for getSurveyById API for surveyId: {} by user: {}", surveyId, currentUserId);

        List<SelectedTopic> selectedTopics = Arrays.asList(
                new SelectedTopic(1L, 101L, "영화보기"),
                new SelectedTopic(1L, 102L,  "클럽가기"),
                new SelectedTopic(2L, 201L, "음악 감상하기")
        );

        SurveyResponse response = new SurveyResponse(
                surveyId,
                currentUserId,
                Instant.now(),
                "COMPANY",
                true,
                "MANY",
                null,
                false,
                false,
                null,
                "FAMILY",
                4,
                selectedTopics
        );
        return ResponseEntity.ok(response);
    }
}