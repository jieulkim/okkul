package site.okkul.be.domain.practice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.dto.QuestionResponse;
import site.okkul.be.domain.practice.docs.PracticeControllerDocs;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.PracticeFeedbackResponse;
import site.okkul.be.domain.practice.dto.response.PracticeStartResponse;

import java.time.Instant;
import java.util.Arrays;

@RestController
@RequestMapping("/practices")
public class PracticeController implements PracticeControllerDocs {

    @Override
    @PostMapping
    public ResponseEntity<PracticeStartResponse> startPractice(
            @PathVariable long surveyId,
            @PathVariable long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        QuestionResponse q1 = new QuestionResponse(1L, 1, "Q1. What?", "https://cdn.cloud...");
        QuestionResponse q2 = new QuestionResponse(2L, 2, "Q2. What?", "https://cdn.cloud...");
        QuestionResponse q3 = new QuestionResponse(3L, 3, "Q3. What?", "https://cdn.cloud...");
        PracticeStartResponse response = new PracticeStartResponse(
                1L,
                Instant.now(),
                30L,
                Arrays.asList(q1, q2, q3)
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @PostMapping(
            value = "/{practiceId}/questions/{questionId}/feedback",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PracticeFeedbackResponse> savePracticeSession(
            @PathVariable long practiceId,
            @PathVariable long questionId,
            @RequestPart("request")PracticeFeedbackRequest request,
            @RequestPart("audio")MultipartFile audioFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }
}
