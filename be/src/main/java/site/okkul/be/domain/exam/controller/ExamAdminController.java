package site.okkul.be.domain.exam.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.exam.service.AdminExamService;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = "ADMIN", description = "ADMIN")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ExamAdminController {

	private final AdminExamService adminExamService;

	@PostMapping("/exam/{examId}/recreate")
	public ResponseEntity<Void> recreateExam(
			@PathVariable Long examId
	) {
		adminExamService.adminExamReportRecreate(examId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/exam/{examId}/answers/{questionOrder}/recreate")
	public ResponseEntity<Void> recreateAnswer(
			@PathVariable Long examId,
			@PathVariable Integer questionOrder
	) {
		adminExamService.adminExamAnswerReportRecreate(examId, questionOrder);
		return ResponseEntity.noContent().build();
	}
}
