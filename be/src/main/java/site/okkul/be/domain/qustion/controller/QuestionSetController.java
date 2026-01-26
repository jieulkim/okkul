package site.okkul.be.domain.qustion.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.qustion.dto.QuestionSetRequest;
import site.okkul.be.domain.qustion.dto.QuestionSetResponse;
import site.okkul.be.domain.qustion.service.QuestionSetService;

/**
 * @author 김남주
 */
@RestController
@RequestMapping("/question-sets")
@RequiredArgsConstructor
public class QuestionSetController implements QuestionSetControllerDocs {

	private final QuestionSetService questionSetService;

	@Override
	@GetMapping
	public ResponseEntity<PagedModel<QuestionSetResponse>> getQuestionSets(
			@ParameterObject @PageableDefault(sort = "id") Pageable pageable
	) {
		return ResponseEntity.ok(
				new PagedModel<>(questionSetService.findAll(pageable)
				)
		);
	}

	@Override
	@GetMapping("/{setId}")
	public ResponseEntity<QuestionSetResponse> getQuestionSet(@PathVariable Long setId) {
		return ResponseEntity.ok(questionSetService.findById(setId));
	}

	@Override
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionSetResponse> createQuestionSet(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionSetRequest request
	) {
		QuestionSetResponse created = questionSetService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(created);
	}

	@Override
	@PatchMapping("/{setId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionSetResponse> updateQuestionSet(
			@PathVariable Long setId,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionSetRequest request
	) {
		return ResponseEntity.ok(questionSetService.update(setId, request));
	}

	@Override
	@DeleteMapping("/{setId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteQuestionSet(
			@PathVariable Long setId,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		questionSetService.delete(setId);
		return ResponseEntity.noContent().build();
	}
}
