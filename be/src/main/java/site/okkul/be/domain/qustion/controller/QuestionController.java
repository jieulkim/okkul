package site.okkul.be.domain.qustion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.qustion.dto.QuestionDetailResponse;
import site.okkul.be.domain.qustion.dto.QuestionRequest;
import site.okkul.be.domain.qustion.service.QuestionService;

/**
 * @author 김남주
 */
@RestController
@RequestMapping("/question-sets/{setId}/questions")
@RequiredArgsConstructor
public class QuestionController implements QuestionControllerDocs {

	private final QuestionService questionService;

	@Override
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionDetailResponse> addQuestion(
			@PathVariable Long setId,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(questionService.addQuestion(setId, request));
	}

	@Override
	@PatchMapping("/{questionId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionDetailResponse> updateQuestion(
			@PathVariable Long setId,
			@PathVariable Long questionId,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionRequest request
	) {
		return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
	}

	@Override
	@DeleteMapping("/{questionId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteQuestion(
			@PathVariable Long setId,
			@PathVariable Long questionId,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		questionService.deleteQuestion(questionId);
		return ResponseEntity.noContent().build();
	}
}
