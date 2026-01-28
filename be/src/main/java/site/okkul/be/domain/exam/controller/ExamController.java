package site.okkul.be.domain.exam.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.docs.ExamControllerDocs;
import site.okkul.be.domain.exam.dto.response.ExamResultResponse;
import site.okkul.be.domain.exam.dto.request.ExamStartRequest;
import site.okkul.be.domain.exam.dto.response.ExamStartResponse;
import site.okkul.be.domain.exam.dto.response.ExamStatusResponse;
import site.okkul.be.domain.exam.dto.response.QuestionResponse;
import site.okkul.be.domain.exam.service.ExamService;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

	/**
	 * 시험 서비스
	 */
	private final ExamService examService;

	@Override
	@PostMapping
	public ResponseEntity<ExamStartResponse> startExam(
			@AuthenticationPrincipal UserDetails user,
			@RequestBody ExamStartRequest request
	) {
		return ResponseEntity.ok(
				examService.startExam(
						Long.parseLong(user.getUsername()),
						request
				)
		);
	}

	/**
	 * 7번 이후 난이도 조정 확정
	 */
	@Override
	@PatchMapping("/{examId}/adjust-level")
	public ResponseEntity<List<QuestionResponse>> getRemainingQuestions(
			@PathVariable Long examId,
			@RequestParam int adjustedDifficulty,
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(
				// TODO: 레벨 수정하기
				null
		);
	}

	/**
	 * 시험정보 가져오기
	 * @param   examId 시험번호
	 */
	@GetMapping("/{examId}")
	public ResponseEntity<List<QuestionResponse>> getExamInfo(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(
				null
		);
	}

	/**
	 * 문제 가져오기
	 */
	@GetMapping("/{examId}/questions")
	public ResponseEntity<List<QuestionResponse>> getQuestions(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(
				null
		);
	}


	/**
	 * 음성 답변 제출
	 * - examId + answerId 검증은 service 내부에서 이미 수행(추가로 해도 됨)
	 */
	@Override
	@PostMapping(
			value = "/{examId}/answers/{answerId}",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<Void> submitAnswer(
			@PathVariable Long examId,
			@PathVariable Long answerId,
			@RequestPart("file") MultipartFile file,
			@AuthenticationPrincipal UserDetails user
	) {
		examService.submitAnswer(examId, answerId, file);
		return ResponseEntity.accepted().build();
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
