package site.okkul.be.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모의고사 최종 결과 응답 데이터")
public class ExamResultResponse {

    @Schema(description = "모의고사 ID", example = "550")
    private Long examId;

    @Schema(description = "시험 제목", example = "2024-03-21 실전 대비 모의고사")
    private String title;

    @Schema(description = "시험 응시 일시", example = "2024-03-25T14:30:00")
    private LocalDateTime createdAt;

    // 1. 총체적 분석 결과 (Exam_reports 테이블 데이터)
    @Schema(description = "시험 총체적 분석 리포트")
    private ExamSummaryResponse summary;

    // 2. 문항별 상세 분석 리스트 (Exam_questions + Type_feedbacks 데이터)
    @Schema(description = "문항별 상세 분석 결과 리스트")
    private List<QuestionResultDetailResponse> questionResults;
}

