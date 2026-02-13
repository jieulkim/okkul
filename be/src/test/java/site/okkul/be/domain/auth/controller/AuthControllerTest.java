package site.okkul.be.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.auth.dto.TokenReissueRequest;
import site.okkul.be.domain.auth.service.JwtProvider;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.OpicLevel;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.entity.UserRole;
import site.okkul.be.domain.user.repository.UserJpaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Auth 컨트룰러 테스트 (통합)")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserJpaRepository userRepository;

	@Autowired
	private JwtProvider jwtProvider;

	private User savedUser;
	private String validRefreshToken;

	@BeforeEach
	void setUp() {
		// 테스트용 사용자 생성
		User user = User.builder()
				.provider(OAuthProvider.GOOGLE)
				.providerId("test-provider-id")
				.name("Test User")
				.email("test@example.com")
				.roles(Set.of(UserRole.USER))
				.currentLevel(OpicLevel.INTERMEDIATE_MID_1)
				.targetLevel(OpicLevel.ADVANCED_LOW)
				.createdAt(Instant.now())
				.build();
		savedUser = userRepository.save(user);

		// 유효한 리프레시 토큰 생성
		validRefreshToken = jwtProvider.createRefreshToken(savedUser.getId());
	}

	@Nested
	@DisplayName("POST /auth/reissue")
	class ReissueToken {

		@Test
		@DisplayName("성공: 유효한 리프레시 토큰으로 요청 시 새로운 액세스 토큰을 반환한다")
		void success() throws Exception {
			// given
			TokenReissueRequest request = new TokenReissueRequest(validRefreshToken);

			// when & then
			mockMvc.perform(post("/auth/reissue")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.accessToken").exists())
					.andDo(print());
		}

		@Test
		@DisplayName("실패: 리프레시 토큰이 유효하지 않거나 만료된 경우 401 Unauthorized를 반환한다")
		void fail_invalid_token() throws Exception {
			// given
			String invalidToken = "invalid-token";
			TokenReissueRequest request = new TokenReissueRequest(invalidToken);

			// when & then
			mockMvc.perform(post("/auth/reissue")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isUnauthorized())
					.andDo(print());
		}

		@Test
		@DisplayName("실패: 사용자를 찾을 수 없는 경우 401 Unauthorized를 반환한다")
		void fail_user_not_found() throws Exception {
			// given
			// 사용자를 삭제하여 토큰은 유효하지만 사용자가 없는 상황 재현
			userRepository.delete(savedUser);

			TokenReissueRequest request = new TokenReissueRequest(validRefreshToken);

			// when & then
			mockMvc.perform(post("/auth/reissue")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isUnauthorized())
					.andDo(print());
		}
	}
}
