package site.okkul.be.domain.exam.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.dto.*;
import site.okkul.be.global.config.SwaggerConfig;

import java.util.List;

@Tag(name = "Exam", description = "모의고사 응시 API (시험 진행 및 음성 제출)")
public interface ExamControllerDocs {

    @Operation(
            summary = "모의고사 시작",
            description = "시험 세션을 생성하고 초기 문항(1~7번)을 반환합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<ExamStartResponse> startExam(@Parameter(hidden = true) UserDetails user, @RequestBody ExamStartRequest request);

    @Operation(
            summary = "나머지 문항 조회",
            description = "난이도 재조정 포인트(7번 이후)에서 나머지 문항 리스트를 한꺼번에 가져옵니다."
    )
    ResponseEntity<List<QuestionResponse>> getRemainingQuestions(
            @PathVariable Long examId, @RequestParam int adjustedDifficulty
    );

    @Operation(
            summary = "음성 답변 제출",
            description = "사용자의 음성 녹음 파일(mp3/m4a 등)을 서버로 업로드합니다."
    )
    ResponseEntity<Void> submitAnswer(
            @PathVariable Long examId,
            @PathVariable Long answerId,
            @RequestPart("file") MultipartFile file
    );

    @Operation(
            summary = "시험 최종 종료",
            description = "모든 답변 제출을 마치고 시험을 종료합니다. 이때 AI 분석이 시작됩니다."
    )
    ResponseEntity<Void> completeExam(@PathVariable Long examId);

    @Operation(summary = "AI 분석 진행 상태 조회", description = "결과 페이지 진입 전, AI 분석이 완료되었는지 확인(Polling)합니다.")
    ResponseEntity<ExamStatusResponse> getExamStatus(@Parameter(description = "시험 ID") @PathVariable Long examId);

    @Operation(summary = "시험 결과 조회", description = "전체 문항의 답변, STT 스크립트 및 AI 피드백 결과를 조회합니다.")
    public ResponseEntity<ExamResultResponse> getExamResult(@PathVariable Long examId);
}
