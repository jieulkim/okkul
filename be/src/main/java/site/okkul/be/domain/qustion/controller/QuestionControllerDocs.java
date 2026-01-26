package site.okkul.be.domain.qustion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import site.okkul.be.domain.qustion.dto.QuestionDetailResponse;
import site.okkul.be.domain.qustion.dto.QuestionRequest;
import site.okkul.be.global.config.SwaggerConfig;

/**
 * @author 김남주
 */
@Tag(name = SwaggerConfig.QUESTION)
public interface QuestionControllerDocs {

	@Operation(summary = "문항 추가 (어드민)", description = "특정 세트에 새로운 문항을 추가합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionDetailResponse> addQuestion(
			@Parameter(description = "세트 ID") Long setId,
			@Parameter(hidden = true) UserDetails userDetails,
			@Parameter(description = "문제 내용") QuestionRequest request
	);

	@Operation(summary = "문항 수정 (어드민)", description = "특정 문항의 텍스트나 음성 URL을 수정합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionDetailResponse> updateQuestion(
			@Parameter(description = "세트 ID") Long setId,
			@Parameter(description = "문항 ID") Long questionId,
			@Parameter(hidden = true) UserDetails userDetails,
			@Parameter(description = "문제 내용") QuestionRequest request
	);

	@Operation(summary = "문항 삭제 (어드민)", description = "세트 내에서 특정 문항을 삭제합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<Void> deleteQuestion(
			@Parameter(description = "세트 ID") Long setId,
			@Parameter(description = "문항 ID") Long questionId,
			@Parameter(hidden = true) UserDetails userDetails
	);
}
