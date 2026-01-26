package site.okkul.be.domain.auth.dto;

import java.util.Map;
import site.okkul.be.domain.user.entity.OAuthProvider;

public interface OAuth2UserInfo {
	String getProviderId();       // 소셜 ID (sub, id 등)

	OAuthProvider getProvider();  // GOOGLE, KAKAO

	String getEmail();

	String getName();

	String getProfileImageUrl();

	Map<String, Object> getAttributes(); // 원본 데이터
}
