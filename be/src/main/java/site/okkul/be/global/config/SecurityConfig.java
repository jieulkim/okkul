package site.okkul.be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.okkul.be.domain.auth.JwtAccessDeniedHandler;
import site.okkul.be.domain.auth.JwtAuthenticationEntryPoint;
import site.okkul.be.domain.auth.dto.OAuth2SuccessHandler;
import site.okkul.be.domain.auth.filter.JwtAuthenticationFilter;
import site.okkul.be.domain.auth.service.CustomOAuth2UserService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// 1. CSRF 비활성화 (JWT 사용 시 일반적으로 끔)
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())

				// 2. 기본 로그인 폼 비활성화 (우리는 소셜 로그인만 함)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)

				// 3. 세션 설정 (JWT를 쓸 것이므로 STATELESS로 설정)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 처리
						.accessDeniedHandler(jwtAccessDeniedHandler)           // 403 처리
				)

				// 4. URL 권한 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/auth/**", "/login/**", "/oauth2/**").permitAll() // ★ /login과 /oauth2 관련 경로 허용
						// 특정 도메인은 GET 다 걸려야 함
						.requestMatchers(HttpMethod.GET, "/exam/**", "/practices/**", "/history/**", "/surveys/**", "/users/**").authenticated()
						// 2. 모든 GET 요청 허용 (상단에 위치할수록 우선순위가 높음)
						.requestMatchers(HttpMethod.GET, "/**").permitAll()
						// 3. 그 외 (POST, PATCH, DELETE 등)는 인증 필요
						.anyRequest().authenticated()
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
