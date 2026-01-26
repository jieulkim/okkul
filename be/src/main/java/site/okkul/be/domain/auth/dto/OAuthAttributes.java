package site.okkul.be.domain.auth.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.entity.UserRole;

@Getter
public class OAuthAttributes {

	private final String nameAttributeKey; // 로그인 진행 시 키가 되는 필드값
	private final OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 구현체

	@Builder
	public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
		this.nameAttributeKey = nameAttributeKey;
		this.oauth2UserInfo = oauth2UserInfo;
	}

	/**
	 * OAuth2User의 속성(Map)을 기반으로 OAuthAttributes 객체를 생성하는 팩토리 메서드
	 */
	public static OAuthAttributes of(OAuthProvider provider, String userNameAttributeName, Map<String, Object> attributes) {
		OAuth2UserInfo oauth2UserInfo = switch (provider) {
			case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
			// case KAKAO -> new KakaoOAuth2UserInfo(attributes); // 추후 추가
			default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + provider);
		};

		return OAuthAttributes.builder()
				.nameAttributeKey(userNameAttributeName)
				.oauth2UserInfo(oauth2UserInfo)
				.build();
	}

	/**
	 * User 엔티티 생성 (최초 가입 시)
	 */
	public User toEntity() {
		return User.builder()
				.provider(oauth2UserInfo.getProvider())
				.providerId(oauth2UserInfo.getProviderId())
				.name(oauth2UserInfo.getName())
				.email(oauth2UserInfo.getEmail())
				.profileImageUrl(oauth2UserInfo.getProfileImageUrl())
				.roles(Set.of(UserRole.USER)) // 기본 권한 설정 (UserRole Enum 필요)
				.currentLevel(null) // 초기값 null
				.targetLevel(null) // 기본 목표 설정 (필요 시)
				.createdAt(Instant.now())
				.build();
	}
}
