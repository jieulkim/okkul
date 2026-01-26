package site.okkul.be.domain.qustion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import site.okkul.be.domain.qustion.dto.QuestionTypeRequest;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.QUESTION, description = "문항 API (문항 세트 및 유형 조회)")
public interface QuestionTypeControllerDocs {

	@Operation(
			summary = "문항 유형 목록 조회",
			description = "페이징 가능한 문항 유형 목록을 반환합니다.\n" +
					"지원 파라미터: `page` (0부터 시작), `size`, `sort`.\n" +
					"응답은 HATEOAS `PagedModel<...>` 형태이며, `Pageable`을 통해 페이지를 제어합니다."
	)
	ResponseEntity<PagedModel<QuestionType>> getQuestionTypes(Pageable pageable);

	@Operation(
			summary = "문항 유형 단건 조회",
			description = "문항 유형 ID로 단건 조회합니다. 존재하지 않으면 404를 반환합니다."
	)
	ResponseEntity<QuestionType> getQuestionType(
			@Parameter(description = "문제 유형 ID") Long id
	);

	@Operation(
			summary = "문항 유형 생성 (어드민 전용)",
			description = "새 문항 유형을 생성합니다. 유효성 검사 후 생성된 리소스의 URI를 `Location` 헤더로 반환합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionType> createQuestionType(
			@Parameter(hidden = true) UserDetails userDetails,
			@Parameter(description = "질문 유형") QuestionTypeRequest questionTypeRequest
	);

	@Operation(
			summary = "문항 유형 수정 (어드민 전용)",
			description = "기존 문항 유형을 수정합니다. 부분 수정 또는 전체 교체를 설명하고, 성공 시 수정된 리소스를 반환합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<QuestionType> updateQuestionType(
			@Parameter(description = "문제 유형 ID") Long id,
			@Parameter(hidden = true) UserDetails userDetails,
			@Parameter(description = "질문 유형") QuestionTypeRequest questionTypeRequest
	);

	@Operation(
			summary = "문항 유형 삭제 (어드민 전용)",
			description = "문항 유형을 삭제합니다. 성공 시 `204 No Content`를 반환합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<Void> deleteQuestionType(
			@Parameter(description = "문제 유형 ID") Long id,
			@Parameter(hidden = true) UserDetails userDetails
	);
}
