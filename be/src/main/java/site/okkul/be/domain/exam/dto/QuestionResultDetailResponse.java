package site.okkul.be.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문항별 상세 분석 결과")
public class QuestionResultDetailResponse {

    @Schema(description = "출제 순서", example = "3")
    private Integer questionOrder;

    @Schema(description = "질문 텍스트")
    private String questionText;

    @Schema(description = "사용자 답변 음성 URL")
    private String audioUrl;

    @Schema(description = "답변 길이(초)", example = "45")
    private Integer duration;

    @Schema(description = "사용자 답변 STT 결과")
    private String sttScript;

    @Schema(description = "AI가 제안하는 교정 스크립트 (오꿀쌤의 교정 스크립트)")
    private String enhancedScript;

    // --- 영역별 상세 피드백 (Type_feedbacks 테이블 기반) ---

    @Schema(description = "문법 분석 결과 (점수 및 피드백)")
    private FeedbackSetResponse grammar; //

    @Schema(description = "어휘 분석 결과 (점수 및 피드백)")
    private FeedbackSetResponse vocabulary; //

    @Schema(description = "논리 구성 분석 결과 (점수 및 피드백)")
    private FeedbackSetResponse logic; //

    @Schema(description = "유창성 분석 결과 (점수 및 피드백)")
    private FeedbackSetResponse fluency; //

    @Schema(description = "주제 적합성 분석 결과 (점수 및 피드백)")
    private FeedbackSetResponse relevance; // 추가 제안된 한 축
}
