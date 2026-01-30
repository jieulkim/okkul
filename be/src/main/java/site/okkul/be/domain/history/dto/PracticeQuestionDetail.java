package site.okkul.be.domain.history.dto;

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
@Schema(description = "연습 내의 개별 질문 정보와 답변 시도 리스트")
public class PracticeQuestionDetail {

    @Schema(description = "문항 ID", example = "7")
    private Long questionId;

    @Schema(description = "질문 순서 (Q1, Q2...)", example = "1")
    private int questionOrder;

    @Schema(description = "질문 내용", example = "당신이 가장 좋아하는 공원에 대해 묘사해주세요.")
    private String questionText;

    @Schema(description = "해당 질문에 대한 답변 시도(Cycles) 리스트")
    private List<PracticeCycleDetail> attempts;
}