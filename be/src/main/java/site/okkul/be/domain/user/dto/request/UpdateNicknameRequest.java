package site.okkul.be.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 닉네임 업데이트 요청 DTO")
public record UpdateNicknameRequest(
		@Schema(description = "변경할 닉네임", example = "new_nickname")
		String nickname
) {
}
