package site.okkul.be.domain.auth.dto;

import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class JwtUserDetails implements UserDetails {

	private final Long userId; // 우리 DB의 PK
	private final Collection<? extends GrantedAuthority> authorities;

	@Override
	public String getPassword() {
		return null; // JWT 방식이라 비밀번호 불필요
	}

	@Override
	public String getUsername() {
		return String.valueOf(userId); // 식별자로 userId 사용
	}
}
