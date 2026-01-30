package site.okkul.be.domain.practice.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.*;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.PRACTICE, description = "유형별 연습 모드 기능 API")
public interface PracticeControllerDocs {

    @Operation(summary = "유형별 연습 모드 생성 API",
            description =
                    "사용자가 유형별 연습 모드를 새로 시작할 때마다 해당 API를 요청합니다." +
                    "사용자가 선택한 소재ID, 설문조사ID를 바탕으로 랜덤으로 문제 세트를 가져오고," +
                    "유형별 연습 모드 데이터를 생성해 ID를 반환합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeCreateResponse> startPractice(
            @Parameter(description = "설문조사 ID", required = true, example = "1") @PathVariable Long surveyId,
            @Parameter(description = "소재 ID", required = true, example = "101") @PathVariable Long topicId,
            @Parameter(description = "문제 유형 ID", required = true, example = "3") @PathVariable Long typeId,
            @Parameter(hidden = true) UserDetails userDetails
    );


    @Operation(summary = "유형별 연습 모드 문제 조회 API",
            description =
                    "해당 유형별 연습 모드에서 할당된 세트와 하위 문제의 상세 정보를 반환합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeQuestionResponse> getPracticeProblem(
            @Parameter(description = "유형연습모드 ID", required = true, example = "1") @PathVariable Long practiceId,
            @Parameter(hidden = true) UserDetails userDetails
    );

    @Operation(summary = "유형별 연습 피드백 생성 요청 API",
            description =
                    "유형 연습 모드에서 특정 문제에 대해서" +
                    "사용자의 답변(한/영 스크립트, 녹음 파일)을 받아" +
                    "AI서버를 통해 AI 피드백을 받아옵니다." +
                    "해당 데이터를 DB에 저장합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeAnswerIdResponse> savePracticeSession(
            @Parameter(description = "AI 사용여부", required = true, example = "false") @RequestParam Boolean ai,
            @Parameter(description = "유형연습 ID", required = true, example = "1") @PathVariable Long practiceId,
            @Parameter(description = "스크립트 정보 (JSON)") @RequestPart("request") PracticeFeedbackRequest request,
            @Parameter(description = "사용자 영어 녹음 파일") @RequestPart("audio") MultipartFile audioFile,
            @Parameter(hidden = true) UserDetails userDetails
    );


    @Operation(summary = "유형별 연습 피드백 결과 조회 API",
            description =
                    "피드백 결과를 조회합니다." +
                        "AI 피드백이 아직 처리중이라면 '처리중'을 의미하는 PROCESSING" +
                        "AI 피드백이 생성완료된 상태라면 '완료'를 의미하는 COMPLETED" +
                        "AI 피드백 생성이 실패되었다면 '실패'를 의미하는 FAILED가 응답됩니다." +
                        "프론트엔드는 PROCESSING 중일 경우, 로딩 표시를 화면에 나타내고," +
                        "COMPLETED를 응답받을 경우 피드백 결과를 화면에 표시하고," +
                        "FAILED를 응답받을 경우 '피드백 생성이 실패했습니다'라고 사용자에게 표시합니다." +
                        "이처럼 피드백 저장 상태에 따라서 분기처리하여 화면에 표시합니다."
    )
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<PracticeAIFeedbackResult> getPracticeFeedback(
            @Parameter(description = "피드백 ID", required = true, example = "1") @PathVariable Long feedbackId,
            @Parameter(hidden = true) UserDetails userDetails
    );
}
