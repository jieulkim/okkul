package site.okkul.be.domain.user.entity;

/**
 * OAuth 제공자 열거형
 * 현재는 Google만 지원
 */
public enum OAuthProvider {
	GOOGLE,
	;

	public static OAuthProvider getProvider(String provider) {
		return OAuthProvider.valueOf(provider.toUpperCase());
	}
}
