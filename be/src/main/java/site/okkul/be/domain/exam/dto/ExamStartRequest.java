package site.okkul.be.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 시작 요청 데이터")
public class ExamStartRequest {

    @NotNull(message = "설문조사 ID는 필수입니다.")
    @Schema(description = "DB에 저장된 사용자의 설문조사 완료 데이터 ID", example = "5001")
    private Long surveyId;
}
