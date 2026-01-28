package site.okkul.be.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.user.dto.request.UpdateNicknameRequest;
import site.okkul.be.domain.user.dto.request.UpdateTargetLevelRequest;
import site.okkul.be.domain.user.dto.response.UserResponse;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserJpaRepository userRepository;
	// private final ImageUploadService imageUploadService; // S3 업로드용 서비스

	/**
	 * 내 정보 조회
	 *
	 * @param userId 조회할 사용자 ID
	 */
	@Transactional(readOnly = true)
	public UserResponse getMyInfo(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
		return UserResponse.from(user);
	}

	/**
	 * 목표 등급 수정
	 *
	 * @param userId  조회할 사용자 ID
	 * @param request 목표 등급 수정 요청 DTO
	 */
	@Transactional
	public UserResponse updateTargetLevel(Long userId, UpdateTargetLevelRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
		user.updateTargetLevel(request.targetLevel()); // 엔티티 메서드 호출
		return UserResponse.from(user);
	}

	/**
	 * 닉네임 수정
	 *
	 * @param userId  조회할 사용자 ID
	 * @param request 닉네임 수정요청 DTO
	 */
	@Transactional
	public UserResponse updateNickname(Long userId, UpdateNicknameRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
		user.updateNickname(request.nickname());
		return UserResponse.from(user);
	}

	/**
	 * 프로필 이미지 수정
	 *
	 * @param userId 조회할 사용자 ID
	 * @param file   업로드할 이미지 파일
	 */
	@Transactional
	public UserResponse updateProfileImage(Long userId, MultipartFile file) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

		// 1. 이미지 업로드 (S3 등) - 구현 필요
		// String imageUrl = imageUploadService.upload(file);
		String imageUrl = "https://dummy-s3-url.com/profile/" + userId + ".jpg"; // 임시 더미 URL

		// 2. 엔티티 정보 업데이트
		user.updateProfileImage(imageUrl);

		return UserResponse.from(user);
	}
}
