package site.okkul.be.domain.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.user.dto.request.UpdateNicknameRequest;
import site.okkul.be.domain.user.dto.request.UpdateTargetLevelRequest;
import site.okkul.be.domain.user.dto.response.UserResponse;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.USER, description = "회원 정보 관리 API")
public interface UserControllerDocs {

	@Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 상세 정보를 조회합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<UserResponse> getMyInfo(
			@Parameter(hidden = true) UserDetails user
	);

	@Operation(summary = "목표 등급(Target Level) 수정", description = "오픽 목표 등급(IM2, AL 등)을 수정합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<UserResponse> updateTargetLevel(
			@Parameter(hidden = true) UserDetails user,
			UpdateTargetLevelRequest request
	);

	@Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 변경합니다.")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<UserResponse> updateNickname(
			@Parameter(hidden = true) UserDetails user,
			UpdateNicknameRequest request
	);

	@Operation(summary = "프로필 이미지 변경", description = "프로필 이미지를 업로드하여 변경합니다. (MultipartFile)")
	@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
	ResponseEntity<UserResponse> updateProfileImage(
			@Parameter(hidden = true) UserDetails user,
			@Parameter(
					description = "업로드할 이미지 파일 (jpg, png)",
					content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
			)
			@RequestPart(value = "file") MultipartFile file
	);
}
