package site.okkul.be.domain.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 */
@Entity
@Table(
		name = "users",
		uniqueConstraints = {
				// 사용자 당 소셜 로그인 제공자와 제공자 ID의 조합은 유일해야 함
				@UniqueConstraint(
						name = "uk_user_provider_provider_id",
						columnNames = {"provider", "providerId"}
				)
		}
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {
	/**
	 * 사용자 고유 ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	/**
	 * 소셜 로그인 제공자 (GOOGLE, KAKAO 등) -> ERD의 'Field'
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OAuthProvider provider;

	/**
	 * 소셜 서비스 고유 ID (sub, id 등)
	 * Oauth에서 활용하는 사용자 식별자
	 */
	@Column(nullable = false, length = 50)
	private String providerId;

	/**
	 * 실명 또는 닉네임
	 */
	@Column(length = 30)
	private String name;

	/**
	 * 프로필 이미지 URL
	 */
	@Column(length = 500)
	private String profileImageUrl;

	/**
	 * 이메일
	 */
	@Column(length = 256)
	private String email;

	/**
	 * 현재 등급 (AL, IH)
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private OpicLevel currentLevel;

	/**
	 * 목표 등급 (AL, IH)
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private OpicLevel targetLevel;

	/**
	 * 사용자 역할 (USER, ADMIN)
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private Set<UserRole> roles;

	/**
	 * 생성 일시
	 */
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/**
	 * 닉네임 변경
	 */
	public void updateNickname(String newNickname) {
		if (newNickname != null && !newNickname.isBlank()) {
			this.name = newNickname;
		}
	}

	/**
	 * 목표 등급 변경
	 */
	public void updateTargetLevel(OpicLevel targetLevel) {
		this.targetLevel = targetLevel;
	}

	/**
	 * 프로필 이미지 URL 변경
	 */
	public void updateProfileImage(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}
}
