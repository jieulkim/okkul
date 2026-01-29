package site.okkul.be.domain.exam.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import site.okkul.be.domain.exam.dto.request.ExamCreateRequest;
import site.okkul.be.domain.exam.dto.request.ExamQuestionAnswerRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.dto.response.ExamResultResponse;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = "Exam", description = "모의고사 응시 API (시험 진행 및 음성 제출)")
public interface ExamControllerDocs {

	/**
	 * 시험 생성
	 *
	 * @param user    생성하려는 유저
	 * @param request 요청 Body
	 * @return 생성된 모의시험 정보
	 */
	@Operation(
			summary = "모의고사 생성",
			description = "시험 세션을 생성하고 초기 문항(1~7번)을 반환합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<ExamDetailResponse> startExam(
			@Parameter(hidden = true) UserDetails user,
			@Parameter(description = "시험 생성 요청 객체") ExamCreateRequest request
	);

	/**
	 * 시험정보 가져오기
	 *
	 * @param examId 시험번호
	 * @param user   유저 정보
	 */
	@Operation(
			summary = "시험 정보 조회(시험 상태 + 문제 Set + 문제들)",
			description = "시험 정보를 조회합니다"
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<ExamDetailResponse> getExamInfo(
			@Parameter(description = "시험 ID") Long examId,
			@Parameter(hidden = true) UserDetails user
	);

	/**
	 * 7번이후 난이도 조정 및 나머지 문제 생성
	 *
	 * @param examId             시험번호
	 * @param adjustedDifficulty 조정된난이도
	 * @param user               유저정보
	 */
	@Operation(
			summary = "난이도 조정 및 나머지 문제 생성",
			description = "난이도 재조정 포인트(7번 이후)에서 난이도를 조정하고 나머지 문항 리스트를 가져옵니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<ExamDetailResponse> updateAdjustedDifficulty(
			@Parameter(description = "시험 ID") Long examId,
			@Parameter(description = "시험 난이도 (new)") Integer adjustedDifficulty,
			@Parameter(hidden = true) UserDetails user
	);

	/**
	 * 음성 답변 제출
	 * - examId + answerId 검증은 service 내부에서 이미 수행(추가로 해도 됨)
	 */
	@Operation(
			summary = "답변 제출",
			description = "사용자의 음성 녹음 파일(mp3/m4a 등)을 서버로 업로드합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<Void> submitAnswer(
			@Parameter(description = "시험 ID") Long examId,
			@Parameter(description = "답변 번호 (1번, 2번...)") Integer questionOrder,
			@Parameter(description = "답변 저장요청 객체") ExamQuestionAnswerRequest examQuestionAnswerRequest,
			@Parameter(hidden = true) UserDetails user
	);

	/**
	 * 시험 종료
	 * - 모든 답변 제출 후 시험을 종료합니다.
	 *
	 * @param examId 시험번호
	 * @param user   유저정보
	 */
	@Operation(
			summary = "시험 최종 종료",
			description = "모든 답변 제출을 마치고 시험을 종료합니다. 이때 AI 분석이 시작됩니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<Void> completeExam(
			@Parameter(description = "시험 ID") Long examId,
			@Parameter(hidden = true) UserDetails user
	);

	/**
	 * 시험 결과 조회
	 *
	 * @param examId 시험번호
	 * @param user   유저정보
	 * @return 시험 결과
	 */
	@Operation(
			summary = "시험 결과 조회",
			description = "전체 문항의 답변, STT 스크립트 및 AI 피드백 결과를 조회합니다."
	)
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<ExamResultResponse> getExamResult(
			@Parameter(description = "시험 ID") Long examId,
			@Parameter(hidden = true) UserDetails user
	);
}
