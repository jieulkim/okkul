package site.okkul.be.domain.practice.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.PracticeFeedbackResponse;
import site.okkul.be.domain.practice.dto.response.PracticeStartResponse;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.PRACTICE, description = "유형별 연습 모드 기능 API")
public interface PracticeControllerDocs {

    @Operation(summary = "유형별 연습 모드 생성 API",
            description =
                    "사용자가 유형별 연습 모드를 새로 시작할 때마다 해당 API를 요청합니다." +
                    "사용자가 선택한 소재ID, 설문조사ID를 PathVriable로 입력하면," +
                    "선택 내용을 바탕으로 랜덤으로 문제 세트를 가져와 해당 문제 세트와 하위 문제의 상세 정보를 반환합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeStartResponse> startPractice(
            @Parameter(description = "설문조사 ID", required = true, example = "1") @PathVariable long surveyId,
            @Parameter(description = "소재 ID", required = true, example = "1") @PathVariable long topicId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(summary = "유형별 연습 피드백 세션 저장 API",
            description =
                    "유형 연습 모드에서 특정 문제에 대해서" +
                    "사용자의 답변(한/영 스크립트, 녹음 파일)을 받아" +
                    "AI서버를 통해 AI 피드백을 받아옵니다." +
                    "해당 데이터를 DB에 저장하고" +
                    "피드백 결과를 프론트엔드에게 전달합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeFeedbackResponse> savePracticeSession(
            @Parameter(description = "유형연습 ID") @PathVariable long practiceId,
            @Parameter(description = "문제 ID") @PathVariable long questionId,
            @Parameter(description = "스크립트 정보 (JSON)") @RequestPart("request") PracticeFeedbackRequest request,
            @Parameter(description = "사용자 영어 녹음 파일") @RequestPart("audio") MultipartFile audioFile,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );
}
