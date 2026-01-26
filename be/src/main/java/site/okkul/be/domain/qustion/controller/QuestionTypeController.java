package site.okkul.be.domain.qustion.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
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
import site.okkul.be.domain.qustion.dto.QuestionTypeRequest;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.service.QuestionTypeService;

/**
 * @author 김남주
 */
@RestController
@RequestMapping("/question-types")
@RequiredArgsConstructor
public class QuestionTypeController implements QuestionTypeControllerDocs {
	private final QuestionTypeService questionTypeService;

	@Override
	@GetMapping
	public ResponseEntity<PagedModel<QuestionType>> getQuestionTypes(
			@ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<QuestionType> page = questionTypeService.findAll(pageable);
		return ResponseEntity.ok(new PagedModel<>(page));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<QuestionType> getQuestionType(@PathVariable Long id) {
		return ResponseEntity.ok(questionTypeService.findById(id));
	}

	@Override
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionType> createQuestionType(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionTypeRequest questionTypeRequest
	) {
		// 임시 생성 로직
		return ResponseEntity.ok(
				questionTypeService.create(questionTypeRequest)
		);
	}

	@Override
	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<QuestionType> updateQuestionType(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody QuestionTypeRequest questionTypeRequest
	) {
		return ResponseEntity.ok(questionTypeService.update(id, questionTypeRequest));
	}

	@Override
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteQuestionType(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		questionTypeService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
