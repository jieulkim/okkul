package site.okkul.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.auth.domain.AuthErrorCode;
import site.okkul.be.domain.auth.dto.AccessTokenResponse;
import site.okkul.be.domain.auth.dto.JwtUserDetails;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;
import site.okkul.be.global.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtProvider jwtProvider;
	private final UserJpaRepository userJpaRepository;

	@Transactional(readOnly = true)
	public AccessTokenResponse reissueAccessToken(String refreshToken) {
		// 1. 리프레시 토큰 검증
		if (!jwtProvider.validateToken(refreshToken)) {
			throw new BusinessException(AuthErrorCode.TOKEN_INVALID_ERROR);
		}

		// 2. 토큰에서 사용자 정보 추출
		Authentication authentication = jwtProvider.getAuthentication(refreshToken);
		JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
		Long userId = userDetails.getUserId();

		// 3. 사용자 조회 (Role 정보 획득)
		User user = userJpaRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_REGISTERED));

		// 4. 새로운 액세스 토큰 발급
		String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRoles());

		return new AccessTokenResponse(newAccessToken);
	}
}
