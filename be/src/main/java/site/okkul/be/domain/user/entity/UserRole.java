package site.okkul.be.domain.user.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * 사용자 역할을 정의하는 열거형
 */
public enum UserRole implements GrantedAuthority {
	/**
	 * 일반 사용자 역할
	 */
	USER,
	/**
	 * 관리자 역할
	 */
	ADMIN;

	@Override
	public String getAuthority() {
		return name();
	}
}
