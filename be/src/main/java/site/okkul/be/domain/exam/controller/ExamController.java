package site.okkul.be.domain.exam.controller;

import java.net.URI;
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
import site.okkul.be.domain.exam.dto.request.ExamCreateRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.dto.response.ExamResultResponse;
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
	 * 시험 생성
	 *
	 * @param user    생성하려는 유저
	 * @param request 요청 Body
	 * @return 생성된 모의시험 정보
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
	 * 7번 이후 난이도 조정 확정
	 */
	@Override
	@PatchMapping("/{examId}/adjust-level")
	public ResponseEntity<ExamDetailResponse> getRemainingQuestions(
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
	 * 시험정보 가져오기
	 *
	 * @param examId 시험번호
	 */
	@Override
	@GetMapping("/{examId}")
	public ResponseEntity<ExamDetailResponse> getExamInfo(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(
				examService.getExamInfo(
						Long.parseLong(user.getUsername()),
						examId
				)
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
	public ResponseEntity<ExamDetailResponse> getExamStatus(
			@PathVariable Long examId,
			@AuthenticationPrincipal UserDetails user
	) {
		// TODO: 실제 서비스 연동 전까지 더미 유지 가능
		ExamDetailResponse dummy = null;
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
