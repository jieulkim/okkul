package site.okkul.be.domain.history.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.okkul.be.domain.exam.entity.AnswerStatus;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.entity.ExamReport;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.exam.repository.ExamReportJpaRepository;
import site.okkul.be.domain.history.dto.ExamAnswerResponse;
import site.okkul.be.domain.history.dto.ExamHistoryDetailResponse;
import site.okkul.be.domain.history.dto.ExamHistorySummary;
import site.okkul.be.domain.history.dto.PracticeHistorySummary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ExamJpaRepository examJpaRepository;
    private final ExamAnswerJpaRepository examAnswerJpaRepository;
    private final ExamReportJpaRepository examReportJpaRepository;

    public Page<ExamHistorySummary> getExamHistories(Long userId, Pageable pageable) {

        // 1) 기준 페이지: Exam 목록
        Page<Exam> page = examJpaRepository.findByUserId(userId, pageable);

        // 2) 현재 페이지 examId들 뽑기
        List<Long> examIds = page.getContent().stream()
                .map(Exam::getId)
                .toList();

        // 3) 해당 examId들의 report를 한 번에 조회 (Page 필요 없음)
        Map<Long, ExamReport> reportMap = examIds.isEmpty()
                ? Collections.emptyMap()
                : examReportJpaRepository.findByIdIn(examIds).stream()
                .collect(Collectors.toMap(
                        ExamReport::getId,   // examId
                        r -> r
                ));

        // 4) Page.map으로 Summary로 변환하면서 report를 붙임
        return page.map(exam -> {
            ExamReport report = reportMap.get(exam.getId());
            String grade = (report == null) ? null : report.getGrade();

            return ExamHistorySummary.from(exam, grade);
        });
    }

    public ExamHistoryDetailResponse getExamHistoryDetail(Long userId, Long examId) {

        Exam exam = examJpaRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모의고사가 존재하지 않습니다."));

        ExamReport report = examReportJpaRepository.findById(examId).orElse(null);

        site.okkul.be.domain.exam.entity.ExamReport reportEntity =
                examReportJpaRepository.findById(examId).orElse(null);

        return ExamHistoryDetailResponse.builder()
                .examId(exam.getId())
                .createdAt(exam.getCreatedAt())
                .endAt(exam.getEndAt())
                .examReport(reportEntity == null ? null : toReportDto(reportEntity))
                .build();
    }

    private ExamHistoryDetailResponse.ExamReport toReportDto(site.okkul.be.domain.exam.entity.ExamReport r) {
        return ExamHistoryDetailResponse.ExamReport.builder()
                .totalScore(r.getTotalScore())
                .grade(r.getGrade())
                .avgGrammar(r.getAvgGrammar())
                .avgVocab(r.getAvgVocab())
                .avgLogic(r.getAvgLogic())
                .avgFluency(r.getAvgFluency())
                .avgRelevance(r.getAvgRelevance())
                .comment(r.getComment())
                .strengthTypes(r.getStrengthType() == null ? List.of() : List.of(r.getStrengthType()))
                .weaknessTypes(r.getWeaknessType() == null ? List.of() : List.of(r.getWeaknessType()))
                .createdAt(r.getCreatedAt())
                .build();
    }

    public ExamAnswerResponse getExamAnswerDetail(Long userId, Long examId, Integer questionOrder) {

        examJpaRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모의고사가 존재하지 않습니다."));

        ExamAnswer answer = examAnswerJpaRepository
                .findById(new ExamAnswer.ExamAnswerId(examId, questionOrder))
                .orElseThrow(() -> new IllegalArgumentException("해당 문항 답변이 존재하지 않습니다."));

        if (answer.getStatus() != AnswerStatus.COMPLETED) {
            throw new IllegalStateException("AI 분석이 완료되지 않았습니다.");
        }
        if (answer.getImprovedAnswer() == null || answer.getImprovedAnswer().isBlank()) {
            throw new IllegalStateException("AI 개선 답변이 존재하지 않습니다.");
        }

        return ExamAnswerResponse.from(answer);
    }

    public Page<PracticeHistorySummary> getPracticeHistories(Long userId, Pageable pageable) {

    }
}
