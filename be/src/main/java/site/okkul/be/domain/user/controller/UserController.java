package site.okkul.be.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.user.dto.request.UpdateNicknameRequest;
import site.okkul.be.domain.user.dto.request.UpdateTargetLevelRequest;
import site.okkul.be.domain.user.dto.response.UserResponse;
import site.okkul.be.domain.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

	private final UserService userService;

	@Override
	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyInfo(
			@AuthenticationPrincipal UserDetails user
	) {
		return ResponseEntity.ok(userService.getMyInfo(
				Long.parseLong(user.getUsername())
		));
	}

	@Override
	@PostMapping("/target-level")
	public ResponseEntity<UserResponse> updateTargetLevel(
			@AuthenticationPrincipal UserDetails user,
			@RequestBody UpdateTargetLevelRequest request
	) {
		return ResponseEntity.ok(
				userService.updateTargetLevel(
						Long.parseLong(user.getUsername()),
						request
				)
		);
	}

	@Override
	@PostMapping("/nickname")
	public ResponseEntity<UserResponse> updateNickname(
			@AuthenticationPrincipal UserDetails user,
			@RequestBody UpdateNicknameRequest request
	) {
		return ResponseEntity.ok(
				userService.updateNickname(
						Long.parseLong(user.getUsername()),
						request)
		);
	}

	@Override
	@PostMapping(value = "/profile-image", consumes = {"multipart/form-data"})
	public ResponseEntity<UserResponse> updateProfileImage(
			@AuthenticationPrincipal UserDetails user,
			@RequestPart("file") MultipartFile file
	) {
		return ResponseEntity.ok(
				userService.updateProfileImage(
						Long.parseLong(user.getUsername()),
						file
				)
		);
	}
}
