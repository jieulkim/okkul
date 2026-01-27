package site.okkul.be.domain.survey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.okkul.be.domain.survey.dto.response.SurveyCreateResponse;
import site.okkul.be.domain.survey.dto.response.SurveyListResponse;
import site.okkul.be.domain.survey.dto.response.SurveyResponse;
import site.okkul.be.domain.survey.docs.SurveyControllerDocs;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.service.SurveyService;
import site.okkul.be.domain.topic.response.SelectedTopics;

@Slf4j
@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController implements SurveyControllerDocs {

    private final SurveyService surveyService;


    @Override
    @PostMapping
    public ResponseEntity<SurveyCreateResponse> createSurvey(
            @RequestBody SurveyCreateRequest surveyCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SurveyCreateResponse response = surveyService.create(surveyCreateRequest, Long.parseLong(userDetails.getUsername()));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<SurveyListResponse> getSurveyList(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SurveyListResponse response = surveyService.findAllByUser(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> getSurveyById(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SurveyResponse response = surveyService.findById(surveyId, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{surveyId}/topics")
    public ResponseEntity<SelectedTopics> getSurveyTopicById(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SelectedTopics response = surveyService.findTopicsBySurveyId(surveyId, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(response);
    }
}