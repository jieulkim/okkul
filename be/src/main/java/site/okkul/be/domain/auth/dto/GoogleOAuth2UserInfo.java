package site.okkul.be.domain.auth.dto;

import java.util.Map;
import site.okkul.be.domain.user.entity.OAuthProvider;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

	private final Map<String, Object> attributes;

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getProviderId() {
		return (String) attributes.get("sub");
	}

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public String getEmail() {
		return (String) attributes.get("email");
	}

	@Override
	public String getName() {
		return (String) attributes.get("name");
	}

	@Override
	public String getProfileImageUrl() {
		return (String) attributes.get("picture");
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
