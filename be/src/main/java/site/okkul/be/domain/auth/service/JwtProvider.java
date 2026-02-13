package site.okkul.be.domain.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import site.okkul.be.domain.auth.dto.JwtUserDetails;
import site.okkul.be.domain.user.entity.UserRole;

@Slf4j
@Component
public class JwtProvider {
	@Value("${app.jwt.secret}")
	private String secretKey;

	@Value("${app.jwt.access-token-expiration}")
	private Duration accessExpiration;

	@Value("${app.jwt.refresh-token-expiration}")
	private Duration refreshExpiration;

	private SecretKey key;

	@PostConstruct
	public void init() {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secretKey);
		} catch (DecodingException ex) {
			keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		}

		if (keyBytes.length < 32) {
			throw new RuntimeException("JWT Secret Key는 32바이트(256비트) 이상이어야 합니다.");
		}

		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Access Token 생성
	 */
	public String createAccessToken(Long userId, Set<UserRole> roles) {
		return createToken(userId, roles, accessExpiration);
	}

	/**
	 * Refresh Token 생성
	 */
	public String createRefreshToken(Long userId) {
		// Refresh Token에는 보통 권한 정보를 담지 않음 (가볍게)
		return createToken(userId, null, refreshExpiration);
	}

	private String createToken(Long userId, Set<UserRole> roles, Duration expiration) {
		Date now = new Date();

		long expirationMillis = expiration.toMillis();

		var builder = Jwts.builder()
				.subject(String.valueOf(userId)) // sub: userId
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMillis))
				.signWith(key); // 최신 버전 서명 방식

		if (roles != null && !roles.isEmpty()) {
			List<String> roleClaims = roles.stream()
					.map(UserRole::name) // getAuthority() 대신 name() 사용 (Enum 이름 자체)
					.toList();

			builder.claim("roles", roleClaims);
		}

		return builder.compact();
	}

	/**
	 * 토큰에서 Authentication 객체 추출 (Filter에서 사용)
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);

		// 1. 권한 정보 가져오기
		List<String> roles = claims.get("roles", List.class);
		Collection<? extends GrantedAuthority> authorities = (roles == null || roles.isEmpty())
				? List.of(new SimpleGrantedAuthority("ROLE_USER"))
				: roles.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // "ROLE_" 접두사 추가
				.toList();


		// UserDetails 객체 생성 (Principal)
		Long userId = Long.parseLong(claims.getSubject());
		JwtUserDetails principal = new JwtUserDetails(userId, authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	/**
	 * 토큰 검증
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.error("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.error("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.error("JWT 토큰이 잘못되었습니다.");
		}
		return false;
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims(); // 만료되어도 정보는 꺼낼 수 있음
		}
	}
}
