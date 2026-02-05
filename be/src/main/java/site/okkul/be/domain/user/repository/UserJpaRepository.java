package site.okkul.be.domain.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

	/**
	 * 소셜 로그인 제공자와 제공자 ID로 사용자 조회
	 *
	 * @param provider   OAuthProvider (GOOGLE, KAKAO 등)
	 * @param providerId 소셜 서비스 고유 ID
	 * @return Optional<User> 존재하면 사용자 반환, 없으면 빈 Optional 반환
	 */
	Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
