package site.okkul.be.domain.auth.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.okkul.be.domain.auth.exception.AuthErrorCode;
import site.okkul.be.domain.auth.service.JwtProvider;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;
import site.okkul.be.global.exception.BusinessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UserJpaRepository userRepository; // ★ DB 조회를 위해 주입

	private final JwtProvider jwtProvider;

	@Value("${app.frontend.url}")
	private String frontendUrl;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

		// 1. Authentication 객체에서 정보 추출
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// ★ 어떤 소셜 로그인인지 확인 (google, kakao)
		OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
		OAuthProvider provider = OAuthProvider.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());

		// ★ 소셜 쪽의 PK 값 (google=sub, kakao=id)
		String providerId = oAuth2User.getName(); // getName()이 PK값을 반환하도록 설정되어 있음

		// 2. DB에서 유저 조회 (여기서 확실하게 가져옵니다)
		User user = userRepository.findByProviderAndProviderId(provider, providerId)
				.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_REGISTERED));

		// 3. 첫 로그인 판단 로직 (User 엔티티 상태로 판단)
		boolean isFirst = (user.getTargetLevel() == null);

		// 4. JWT 생성
		String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRoles());
		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		// 5. 프론트 리다이렉트
		// TODO: 프론트팀과 협의하여 URL 및 파라미터 결정
		String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
				.queryParam("accessToken", accessToken)
				.queryParam("refreshToken", refreshToken)
				.queryParam("isFirst", isFirst)
				.build().toUriString();

		response.sendRedirect(targetUrl);
	}
}
