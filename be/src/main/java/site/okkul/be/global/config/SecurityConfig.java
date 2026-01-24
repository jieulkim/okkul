package site.okkul.be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.okkul.be.domain.auth.dto.OAuth2SuccessHandler;
import site.okkul.be.domain.auth.filter.JwtAuthenticationFilter;
import site.okkul.be.domain.auth.service.CustomOAuth2UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// 1. CSRF 비활성화 (JWT 사용 시 일반적으로 끔)
				.csrf(AbstractHttpConfigurer::disable)

				// 2. 기본 로그인 폼 비활성화 (우리는 소셜 로그인만 함)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)

				// 3. 세션 설정 (JWT를 쓸 것이므로 STATELESS로 설정)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				// 4. URL 권한 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.anyRequest().authenticated() // 그 외는 인증 필요
				)

				// 5. OAuth2 로그인 설정 (★ 여기가 핵심)
				.oauth2Login(oauth2 -> oauth2
						// 로그인 성공 시 리다이렉트나 처리를 할 핸들러
						.successHandler(oAuth2SuccessHandler)
						// 사용자 정보 엔드포인트 설정
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService)
						)
				)

				// 6. JWT 인증 필터 추가
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
		;

		return http.build();
	}
}
