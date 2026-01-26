package site.okkul.be.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Builder;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.entity.UserRole;

@Builder
@Schema(description = "회원 정보 응답 DTO")
public record UserResponse(
		@Schema(description = "회원 고유 아이디", example = "1")
		Long id,
		@Schema(description = "연동 제공자", example = "GOOGLE")
		String provider,
		@Schema(description = "회원 이름", example = "홍길동")
		String name,
		@Schema(description = "프로필 이미지 URL", example = "https://example.com/avatar.png")
		String profileImageUrl,
		@Schema(description = "이메일 주소", example = "user@example.com")
		String email,
		@Schema(description = "현재 레벨 (Enum 이름)", example = "BEGINNER")
		String currentLevel,
		@Schema(description = "목표 레벨 (Enum 이름)", example = "ADVANCED")
		String targetLevel,
		@Schema(description = "권한 목록", example = "[\"ROLE_USER\",\"ROLE_ADMIN\"]")
		Set<UserRole> roles
) {
	public static UserResponse from(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.provider(user.getProvider() != null ? user.getProvider().name() : null)
				.name(user.getName())
				.profileImageUrl(user.getProfileImageUrl())
				.email(user.getEmail())
				.currentLevel(user.getCurrentLevel() != null ? user.getCurrentLevel().name() : null)
				.targetLevel(user.getTargetLevel() != null ? user.getTargetLevel().name() : null)
				.roles(user.getRoles())
				.build();
	}
}
