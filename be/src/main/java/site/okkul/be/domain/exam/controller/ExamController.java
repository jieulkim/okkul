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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.exam.docs.ExamControllerDocs;
import site.okkul.be.domain.exam.dto.request.ExamCreateRequest;
import site.okkul.be.domain.exam.dto.request.ExamQuestionAnswerRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.service.ExamAnswerService;
import site.okkul.be.domain.exam.service.ExamService;
import site.okkul.be.global.config.SwaggerConfig;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

	/**
	 * 시험 서비스
	 */
	private final ExamService examService;

	private final ExamAnswerService examAnswerService;

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

		// 문제 할당
		examAnswerService.allocateQuestion(exam.id());

		// 문제 반환
		return ResponseEntity.created(
				URI.create("/exam/" + exam.id())
		).body(
				examService.getExamInfoDetails(
						Long.parseLong(user.getUsername()),
						exam.id()
				).questionSubList(1, 8)
		);
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
		// 시험 난이도 조절
		examService.updateLevel(
				Long.parseLong(user.getUsername()),
				examId,
				adjustedDifficulty
		);
		// 문제 할당
		examAnswerService.allocateQuestion(examId);

		// 문제 반환
		return ResponseEntity.ok(
				examService.getExamInfoDetails(
						Long.parseLong(user.getUsername()),
						examId
				).questionSubList(8, 15)
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
			@AuthenticationPrincipal UserDetails user,
			@RequestHeader(value = SwaggerConfig.REAL_AI_USE, defaultValue = "false") boolean useRealAi
	) {
		examAnswerService.submitAnswer(examId, questionOrder, examQuestionAnswerRequest, Long.parseLong(user.getUsername()));
		examAnswerService.feedbackAnswer(examId, questionOrder, useRealAi);
		return ResponseEntity.accepted().build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostMapping("/{examId}/complete")
	public ResponseEntity<Void> completeExam(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user,
			@RequestHeader(value = SwaggerConfig.REAL_AI_USE, defaultValue = "false") boolean useRealAi
	) {
		examService.completeExam(examId);
		examService.examCreateReport(examId, useRealAi);
		return ResponseEntity.ok().build();
	}
}
