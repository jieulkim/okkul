package site.okkul.be.domain.history.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.okkul.be.domain.history.dto.*;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = "History", description = "히스토리 API")
public interface HistoryControllerDocs {

    @Operation(
            summary = "모의고사 히스토리 목록 조회",
            description = "사용자가 응시한 모의고사 기록 목록을 최신순으로 조회한다"
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PagedModel<ExamHistorySummary>>  getExamHistories(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @Parameter(hidden = true) UserDetails user
    );

    @Operation(
            summary = "모의고사 히스토리 상세 조회",
            description = "특정 모의고사(examId)에 대한 상세 정보를 조회한다"
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<ExamHistoryDetailResponse> getExamHistoryDetail(
            @Parameter(description = "모의고사 ID(exam_id)") @PathVariable Long examId,
            @Parameter(hidden = true) UserDetails user
    );

    @Operation(
            summary = "모의고사 문항(답변) 상세 조회",
            description = "특정 모의고사(examId)의 특정 문항 순서(question_order)에 대한 상세 피드백을 조회한다"
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<ExamAnswerResponse> getExamAnswerDetail(
            @Parameter(description = "모의고사 ID(exam_id)") @PathVariable Long examId,
            @Parameter(description = "문항 순서(question_order)") @PathVariable Integer questionOrder,
            @Parameter(hidden = true) UserDetails user
    );
}
