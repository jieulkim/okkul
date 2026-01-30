package site.okkul.be.domain.exam.controller;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.exam.docs.ExamControllerDocs;
import site.okkul.be.domain.exam.dto.request.ExamCreateRequest;
import site.okkul.be.domain.exam.dto.request.ExamQuestionAnswerRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.service.ExamService;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

	/**
	 * 시험 서비스
	 */
	private final ExamService examService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostMapping
	public ResponseEntity<ExamDetailResponse> startExam(
			@AuthenticationPrincipal UserDetails user,
			@RequestBody ExamCreateRequest request
	) {
		// 문제 생성
		ExamDetailResponse exam = examService.createExam(
				Long.parseLong(user.getUsername()),
				request.surveyId()
		);
		// 비동기로 문제 할당
		examService.allocateQuestion(exam.id());

		return ResponseEntity.created(
				URI.create("/exam/" + exam.id())
		).body(exam);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GetMapping("/{examId}")
	public ResponseEntity<ExamDetailResponse> getExamInfo(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(
				examService.getExamInfoDetails(
						Long.parseLong(user.getUsername()),
						examId
				)
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PatchMapping("/{examId}/adjust-level")
	public ResponseEntity<ExamDetailResponse> updateAdjustedDifficulty(
			@PathVariable Long examId,
			@RequestParam Integer adjustedDifficulty,
			@AuthenticationPrincipal UserDetails user
	) {
		ExamDetailResponse exam = examService.updateLevel(
				Long.parseLong(user.getUsername()),
				examId,
				adjustedDifficulty
		);
		examService.allocateQuestion(exam.id());

		return ResponseEntity.ok(
				exam
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostMapping(
			value = "/{examId}/answers/{questionOrder}",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<Void> submitAnswer(
			@PathVariable Long examId,
			@PathVariable Integer questionOrder,
			@ModelAttribute ExamQuestionAnswerRequest examQuestionAnswerRequest,
			@AuthenticationPrincipal UserDetails user
	) {
		examService.submitAnswer(examId, questionOrder, examQuestionAnswerRequest, Long.parseLong(user.getUsername()));
		return ResponseEntity.accepted().build();
	}

	/**
	 * {@inheritDoc}
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
}
