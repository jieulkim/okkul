package site.okkul.be.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import site.okkul.be.domain.user.entity.OpicLevel;

@Schema(description = "회원 목표 등급 업데이트 요청 DTO")
public record UpdateTargetLevelRequest(
		@Schema(description = "변경할 목표 등급", example = "INTERMEDIATE_HIGH")
		OpicLevel targetLevel
) {
}
