package site.okkul.be.domain.practice.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import site.okkul.be.domain.practice.dto.response.PracticeStartResponse;

@Tag(name = "유형별 연습", description = "유형별 연습 모드 기능 API")
public interface PracticeControllerDocs {

    @Operation(summary = "유형별 연습 모드 생성 API",
            description =
                    "사용자가 유형별 연습 모드를 새로 시작할 때마다 해당 API를 요청합니다." +
                    "사용자가 선택한 소재ID, 설문조사ID를 PathVriable로 입력하면," +
                    "선택 내용을 바탕으로 랜덤으로 문제 세트를 가져와 해당 문제 세트와 하위 문제의 상세 정보를 반환합니다."
    )
    ResponseEntity<PracticeStartResponse> startPractice(
            @Parameter(description = "설문조사 ID", required = true, example = "1") @PathVariable long surveyId,
            @Parameter(description = "소재 ID", required = true, example = "1") @PathVariable long topicId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );



}
