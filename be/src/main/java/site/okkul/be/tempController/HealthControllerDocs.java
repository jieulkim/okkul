package site.okkul.be.tempController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "헬스체크", description = "서버의 상태를 확인하는 API")
public interface HealthControllerDocs {

    @Operation(summary = "서버 헬스 체크", description = "서버가 정상적으로 동작하는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 정상 동작"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    ResponseEntity<String> healthCheck();
}
