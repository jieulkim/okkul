package site.okkul.be.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.auth.dto.AccessTokenResponse;
import site.okkul.be.domain.auth.dto.TokenReissueRequest;
import site.okkul.be.domain.auth.service.AuthService;
import site.okkul.be.global.config.SwaggerConfig;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.AUTH, description = "인증 및 토큰 관리 API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/reissue")
	@Operation(
			summary = "액세스 토큰 재발급",
			description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.<br>" +
					"body에 리프래쉬 토큰만 담아주세요"
	)
	public ResponseEntity<AccessTokenResponse> reissue(
			@RequestBody TokenReissueRequest request
	) {
		return ResponseEntity.ok(
				authService.reissueAccessToken(request.refreshToken())
		);
	}
}
