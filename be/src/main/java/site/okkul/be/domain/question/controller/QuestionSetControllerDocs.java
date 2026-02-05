package site.okkul.be.domain.question.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import site.okkul.be.domain.question.dto.QuestionSetRequest;
import site.okkul.be.domain.question.dto.QuestionSetResponse;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.QUESTION)
public interface QuestionSetControllerDocs {

	@Operation(summary = "문항 세트 목록 조회", description = "전체 문항 세트를 페이징하여 조회합니다.")
	ResponseEntity<PagedModel<QuestionSetResponse>> getQuestionSets(
			@ParameterObject Pageable pageable
	);

	@Operation(summary = "문항 세트 단건 조회")
	ResponseEntity<QuestionSetResponse> getQuestionSet(
			@Parameter(description = "세트 ID") Long id
	);

	@Operation(summary = "문항 세트 생성 (어드민)")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionSetResponse> createQuestionSet(
			UserDetails userDetails,
			QuestionSetRequest request
	);

	@Operation(summary = "문항 세트 수정 (어드민)")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionSetResponse> updateQuestionSet(
			Long id,
			UserDetails userDetails,
			QuestionSetRequest request
	);

	@Operation(summary = "문항 세트 삭제 (어드민)")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<Void> deleteQuestionSet(
			Long id,
			UserDetails userDetails
	);
}
