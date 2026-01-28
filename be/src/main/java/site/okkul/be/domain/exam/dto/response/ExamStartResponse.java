package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 시작 응답 데이터")
public class ExamStartResponse {

    @Schema(description = "생성된 모의고사 ID", example = "1")
    private Long examId;

    @Schema(description = "전체 문항 수", example = "15")
    private Integer totalQuestions;

    @Schema(description = "초기 문항 리스트 (1~7번)")
    private List<QuestionResponse> questions;
}
