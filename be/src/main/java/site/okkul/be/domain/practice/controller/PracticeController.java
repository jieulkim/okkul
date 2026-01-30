package site.okkul.be.domain.practice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.practice.docs.PracticeControllerDocs;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.*;
import site.okkul.be.domain.practice.service.PracticeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/practices")
public class PracticeController implements PracticeControllerDocs {

    private final PracticeService practiceService;

    @Override
    @PostMapping
    public ResponseEntity<PracticeCreateResponse> startPractice(
            @RequestParam Long surveyId,
            @RequestParam Long topicId,
            @RequestParam Long typeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(
                practiceService.create(surveyId, topicId, typeId, Long.parseLong(userDetails.getUsername())),
                HttpStatus.CREATED
        );
    }

    @Override
    @GetMapping("/{practiceId}")
    public ResponseEntity<PracticeQuestionResponse> getPracticeProblem(
            @PathVariable Long practiceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(
                practiceService.getPractice(practiceId, Long.parseLong(userDetails.getUsername())),
                HttpStatus.OK);
    }

    /**
     * 답변을 제출하고 AI 피드백을 비동기적으로 요청합니다.
     */
    @Override
    @PostMapping(
            value = "/{practiceId}/feedback",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PracticeAnswerIdResponse> savePracticeSession(
            @PathVariable Long practiceId,
            @RequestPart("request") PracticeFeedbackRequest request,
            @RequestPart("audio") MultipartFile audioFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 서비스는 PracticeAnswer를 생성하고, 백그라운드에서 AI 요청을 트리거한 후, 생성된 answerId를 즉시 반환합니다.
        Long practiceAnswerId = practiceService.createAnswerAndRequestFeedbackAsync(
                practiceId, request, audioFile, Long.parseLong(userDetails.getUsername()));

        // 클라이언트에게는 생성된 answerId를 "요청이 접수되었다"는 의미의 202 Accepted 상태와 함께 반환합니다.
        return new ResponseEntity<>(new PracticeAnswerIdResponse(practiceAnswerId), HttpStatus.ACCEPTED);
    }

    /**
     * practiceAnswerId로 피드백 처리 상태와 결과를 조회합니다.
     */
    @Override
    @GetMapping(value = "/feedback/{practiceAnswerId}")
    public ResponseEntity<PracticeAIFeedbackResult> getPracticeFeedback(
            @PathVariable Long practiceAnswerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 서비스는 피드백 상태를 확인하고, 상태에 따라 다른 객체(PracticeFeedbackResponse 또는 FeedbackStatusResponse)를 반환합니다.
        PracticeAIFeedbackResult result = practiceService
                .getFeedbackResult(practiceAnswerId, Long.parseLong(userDetails.getUsername()));
        
        return ResponseEntity.ok(result);
    }
}
