package site.okkul.be.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.global.config.SwaggerConfig;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	@GetMapping("/me")
	@Operation(summary = "내 정보 조회", description = "토큰을 기반으로 내 정보를 조회합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {

		// ★ 이렇게 바로 꺼내 쓸 수 있습니다!
		Long userId = Long.valueOf(userDetails.getUsername());

		return ResponseEntity.ok("당신의 ID는: " + userId);
	}
}
