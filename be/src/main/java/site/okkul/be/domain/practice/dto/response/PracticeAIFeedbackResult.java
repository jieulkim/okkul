package site.okkul.be.domain.practice.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "AI 피드백 결과 상세")
public class PracticeAIFeedbackResult {

    @Schema(description = "문법 및 어휘 교정 제안 목록")
    private List<ScriptCorrection> scriptCorrections;

    @Schema(description = "종합 평가 코멘트", example = "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다.")
    private String overallComment;
}
