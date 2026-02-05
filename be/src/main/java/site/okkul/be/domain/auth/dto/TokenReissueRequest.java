package site.okkul.be.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 요청")
public record TokenReissueRequest(
		@Schema(
				description = "리프레시 토큰",
				requiredMode = Schema.RequiredMode.REQUIRED,
				example = "eyJhbG..."
		)
		String refreshToken
) {
}
