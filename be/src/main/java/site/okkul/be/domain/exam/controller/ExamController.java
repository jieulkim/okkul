package site.okkul.be.domain.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.docs.ExamControllerDocs;
import site.okkul.be.domain.exam.dto.*;
import site.okkul.be.domain.exam.service.ExamService;

import java.util.List;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

    private final ExamService examService;

    @Override
    @PostMapping("/start")
    public ResponseEntity<ExamStartResponse> startExam(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ExamStartRequest request
    ) {
        Long userId = Long.parseLong(user.getUsername());
        ExamStartResponse response = examService.startExam(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 7번 이후 난이도 조정 확정 후 8번~마지막 문항 생성/반환
     */
    @Override
    @PostMapping("/{examId}/questions/current")
    public ResponseEntity<List<QuestionResponse>> getRemainingQuestions(
            @PathVariable Long examId,
            @RequestParam int adjustedDifficulty,
            @AuthenticationPrincipal UserDetails user
    ) {
        List<QuestionResponse> remaining = examService.getRemainingQuestions(examId, adjustedDifficulty);
        return ResponseEntity.ok(remaining);
    }

    /**
     * 음성 답변 제출
     * - examId + answerId 검증은 service 내부에서 이미 수행(추가로 해도 됨)
     */
    @Override
    @PostMapping(
            value = "/{examId}/questions/{answerId}/answer",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long examId,
            @PathVariable Long answerId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        examService.submitAnswer(examId, answerId, file);
        return ResponseEntity.ok().build();
    }

    /**
     * 시험 종료
     */
    @Override
    @PostMapping("/{examId}/complete")
    public ResponseEntity<Void> completeExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails user
    ) {
        examService.completeExam(examId);
        return ResponseEntity.ok().build();
    }

    // 아래 status/result는 아직 서비스가 더미라면 유지 가능
    // 다만 URL 중복만 고쳐야 함.

    @Override
    @GetMapping("/{examId}/status")
    public ResponseEntity<ExamStatusResponse> getExamStatus(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails user
    ) {
        // TODO: 실제 서비스 연동 전까지 더미 유지 가능
        ExamStatusResponse dummy = ExamStatusResponse.builder()
                .completedQuestions(12)
                .totalQuestions(15)
                .isAllAnalyzed(false)
                .estimatedRemainingSeconds(15)
                .build();

        return ResponseEntity.ok(dummy);
    }

    @Override
    @GetMapping("/{examId}/result")
    public ResponseEntity<ExamResultResponse> getExamResult(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails user) {
        // TODO: 실제 서비스 연동 전까지 더미 유지 가능
        return ResponseEntity.ok().build();
    }
}
