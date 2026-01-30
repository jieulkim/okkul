package site.okkul.be.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamReport;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExamHistorySummary {
    private Long examId;
    private Instant createdAt;
    private Instant endAt;
    private Integer initialDifficulty;  // 초기 설정 난이도
    private Integer adjustedDifficulty; // 7번 문항 이후 변경한 난이도
    private String grade;   // 예측 등급

    public static ExamHistorySummary from(Exam exam, String grade) {
        return new ExamHistorySummary(
                exam.getId(),
                exam.getCreatedAt(),
                exam.getEndAt(),
                exam.getInitialDifficulty(),
                exam.getAdjustedDifficulty(),
                grade
        );
    }
}
