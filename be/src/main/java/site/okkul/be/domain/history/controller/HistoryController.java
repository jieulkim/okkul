package site.okkul.be.domain.history.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.okkul.be.domain.history.docs.HistoryControllerDocs;
import site.okkul.be.domain.history.dto.*;
import site.okkul.be.domain.history.service.HistoryService;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController implements HistoryControllerDocs {

    private final HistoryService historyService;

    @Override
    @GetMapping("/exams")
    public ResponseEntity<PagedModel<ExamHistorySummary>> getExamHistories(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @AuthenticationPrincipal UserDetails user
    ) {
        Page<ExamHistorySummary> response = historyService.getExamHistories(Long.parseLong(user.getUsername()), pageable);
        return ResponseEntity.ok(
                new PagedModel<>(response)
        );
    }

    @Override
    @GetMapping("/exams/{examId}")
    public ResponseEntity<ExamHistoryDetailResponse> getExamHistoryDetail(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails user
    ) {
        ExamHistoryDetailResponse response = historyService.getExamHistoryDetail(Long.parseLong(user.getUsername()), examId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/exams/{examId}/answers/{questionOrder}")
    public ResponseEntity<ExamAnswerResponse> getExamAnswerDetail(
            @PathVariable Long examId,
            @PathVariable Integer questionOrder,
            @AuthenticationPrincipal UserDetails user
    ) {
        ExamAnswerResponse response = historyService.getExamAnswerDetail(Long.parseLong(user.getUsername()), examId, questionOrder);
        return ResponseEntity.ok(response);
    }
}
