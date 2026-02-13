package site.okkul.be.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "액세스 토큰 응답")
public record AccessTokenResponse(
		@Schema(description = "새로 발급된 액세스 토큰", example = "eyJhbG...")
		String accessToken
) {
}
