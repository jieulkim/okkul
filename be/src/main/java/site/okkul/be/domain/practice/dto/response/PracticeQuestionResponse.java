package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PracticeQuestionResponse {
    @Schema(description = "할당된 문제 세트의 ID", example = "10")
    private Long setId;

    @Schema(description = "연습에 포함된 질문 목록")
    private List<PracticeQuestionInfo> questions;
}
