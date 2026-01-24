package site.okkul.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.auth.dto.OAuthAttributes;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserJpaRepository userRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// 1. 구글/카카오에서 유저 정보 가져오기
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		// 2. 어떤 서비스인지(google, kakao) 구분
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuthProvider provider = OAuthProvider.getProvider(registrationId.toUpperCase()); // "google" -> GOOGLE

		String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		// 3. 팩토리 메서드를 통해 OAuthAttributes 생성 (내부에서 Google/Kakao 구현체 선택됨)
		OAuthAttributes attributes = OAuthAttributes.of(provider, userNameAttributeName, oAuth2User.getAttributes());

		// 4. 저장 또는 업데이트
		User user = saveOrUpdate(attributes);

		// 5. UserDetails와 비슷한 OAuth2User 반환 (SecurityContext에 저장됨)
		// 여기서 반환된 객체가 나중에 Controller에서 @AuthenticationPrincipal로 꺼내 쓰는 객체입니다.
		return new DefaultOAuth2User(
				user.getRoles(),
				attributes.getOauth2UserInfo().getAttributes(), // 원본 Map
				attributes.getNameAttributeKey()
		);
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		// 인터페이스를 통해 데이터를 통일성 있게 가져옴
		OAuthProvider provider = attributes.getOauth2UserInfo().getProvider();
		String providerId = attributes.getOauth2UserInfo().getProviderId();

		return userRepository.findByProviderAndProviderId(provider, providerId)
				.map(entity -> {
					log.info("✅ [OAuth2 Service] 기존 사용자 발견: {}", entity.getName());
					// entity.update(...) // 정보 변경 시 여기서 업데이트 (Dirty Checking)
					return entity;
				})
				.orElseGet(() -> {
					log.info("🆕 [OAuth2 Service] 신규 회원가입 진행! 저장합니다.");
					User newUser = attributes.toEntity();
					return userRepository.save(newUser); // 여기서 INSERT 쿼리 발생
				});
	}
}
