package site.okkul.be.domain.history.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.okkul.be.domain.exam.entity.ExamAnswer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 문항(답변) 상세 피드백 응답")
public class ExamAnswerResponse {

    @Schema(description = "모의고사 ID", example = "12")
    private Long examId;

    @Schema(description = "답변 ID", example = "301")
    private Long answerId;

    @Schema(description = "시험 내 문항 순서", example = "3")
    private Integer questionOrder;

    @Schema(description = "STT로 변환된 원본 답변 스크립트")
    private String sttScript;

    @Schema(description = "AI 개선 답변 텍스트")
    private String improvedAnswer;

    private CategoryFeedback categoryFeedback;

    private List<SentenceFeedback> sentenceFeedbacks;

    @Schema(description = "답변 생성 일시")
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "항목별 피드백")
    public static class CategoryFeedback {

        @Schema(description = "주제 적합성 피드백")
        private String relevanceFeedback;

        @Schema(description = "논리성 피드백")
        private String logicFeedback;

        @Schema(description = "유창성 피드백")
        private String fluencyFeedback;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "문장 단위 교정 피드백")
    public static class SentenceFeedback {

        @Schema(description = "문장 피드백 ID", example = "9001")
        private Long feedbackId;

        @Schema(description = "원본 문장")
        private String targetSentence;

        @Schema(description = "문제 구간")
        private String targetSegment;

        @Schema(description = "교정된 표현")
        private String correctedSegment;

        @Schema(description = "피드백 텍스트")
        private String comment;

        @Schema(description = "문장 순서", example = "1")
        private Integer sentenceOrder;

        public static SentenceFeedback from(site.okkul.be.domain.exam.entity.ExamSentenceFeedback f) {
            return SentenceFeedback.builder()
                    .feedbackId(f.getId())
                    .targetSentence(f.getTargetSentence())
                    .targetSegment(f.getTargetSegment())
                    .correctedSegment(f.getCorrectedSegment())
                    .comment(f.getComment())
                    .sentenceOrder(f.getSentenceOrder())
                    .build();
        }
    }

    public static ExamAnswerResponse from(ExamAnswer answer) {
        return ExamAnswerResponse.builder()
                .examId(answer.getId().getExamId())
                .questionOrder(answer.getId().getQuestionOrder())
                .sttScript(answer.getSttScript())
                .improvedAnswer(answer.getImprovedAnswer())
                .categoryFeedback(toCategoryFeedback(answer.getAnswerFeedbacks()))
                .sentenceFeedbacks(
                        answer.getSentenceFeedbacks() == null
                                ? List.of()
                                : answer.getSentenceFeedbacks().stream()
                                .map(SentenceFeedback::from)
                                .toList()
                )
                .createdAt(answer.getCreatedAt())
                .build();
    }

    private static CategoryFeedback toCategoryFeedback(site.okkul.be.domain.exam.entity.ExamAnswerFeedback f) {
        if (f == null) return null;

        return CategoryFeedback.builder()
                .relevanceFeedback(f.getRelevanceFeedback())
                .logicFeedback(f.getLogicFeedback())
                .fluencyFeedback(f.getFluencyFeedback())
                .build();
    }
}
