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
import site.okkul.be.domain.history.domain.HistoryErrorCode;
import site.okkul.be.domain.history.dto.*;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.entity.PracticeSentenceFeedback;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.global.exception.BusinessException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ExamJpaRepository examJpaRepository;
    private final ExamAnswerJpaRepository examAnswerJpaRepository;
    private final ExamReportJpaRepository examReportJpaRepository;
    private final PracticeJpaRepository practiceRepository;
    private final PracticeAnswerJpaRepository practiceAnswerRepository;

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
                .orElseThrow(() -> new BusinessException(HistoryErrorCode.EXAM_HISTORY_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(HistoryErrorCode.EXAM_HISTORY_NOT_FOUND));

        ExamAnswer answer = examAnswerJpaRepository
                .findById(new ExamAnswer.ExamAnswerId(examId, questionOrder))
                .orElseThrow(() -> new BusinessException(HistoryErrorCode.EXAM_ANSWER_NOT_FOUND));

        if (answer.getStatus() != AnswerStatus.COMPLETED) {
            throw new BusinessException(HistoryErrorCode.AI_ANALYSIS_NOT_COMPLETED);
        }
        if (answer.getImprovedAnswer() == null || answer.getImprovedAnswer().isBlank()) {
            throw new BusinessException(HistoryErrorCode.AI_IMPROVED_ANSWER_NOT_FOUND);
        }

        return ExamAnswerResponse.from(answer);
    }

    public Page<PracticeHistorySummary> getPracticeHistories(Long userId, Pageable pageable) {
        // 사용자 ID로 Practice 목록 조회
        Page<Practice> page = practiceRepository.findAllByUserId(userId, pageable);

        // Entity -> DTO 변환
        return page.map(practice -> PracticeHistorySummary.builder()
                .practiceId(practice.getPracticeId())
                .startedAt(practice.getStartedAt())
                .topicId(practice.getTopic().getId())
                .topic(practice.getTopic().getTopicName())
                .typeId(practice.getQuestionType().getId())
                .typeName(practice.getQuestionType().getTypeCode())
                .build());
    }

    public PracticeHistoryDetailResponse getPracticeHistoryDetail(Long userId, Long practiceId) {
        // 1. 연습 기록 조회
        Practice practice = practiceRepository.findByPracticeIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new BusinessException(HistoryErrorCode.PRACTICE_HISTORY_NOT_FOUND));

        // 2. 답변 목록 조회 (N+1 방지 Fetch Join)
        List<PracticeAnswer> answers = practiceAnswerRepository.findAllByPracticeIdWithFeedbacks(practiceId);

        // 3. 답변 정렬 (오래된 순)
        List<PracticeAnswer> sortedAnswers = answers.stream()
                .sorted(Comparator.comparing(PracticeAnswer::getCreatedAt))
                .toList();

        // 4. [핵심] 질문별로 그룹핑 (LinkedHashMap 사용)
        Map<Long, List<PracticeAnswer>> answersByQuestionMap = new LinkedHashMap<>();

        for (PracticeAnswer answer : sortedAnswers) {
            answersByQuestionMap
                    .computeIfAbsent(answer.getQuestion().getId(), k -> new ArrayList<>())
                    .add(answer);
        }

        // 5. 그룹핑된 데이터를 DTO로 변환
        List<PracticeQuestionDetail> questionDetails = new ArrayList<>();
        int qOrder = 1; // 질문 순서 (Q1, Q2...)

        for (Long questionId : answersByQuestionMap.keySet()) {
            List<PracticeAnswer> questionAnswers = answersByQuestionMap.get(questionId);

            // 질문 내용은 첫 번째 답변에서 꺼냄
            site.okkul.be.domain.qustion.entity.Question questionEntity = questionAnswers.get(0).getQuestion();

            // 해당 질문에 대한 시도(Attempt) 리스트 생성
            List<PracticeCycleDetail> attempts = new ArrayList<>();
            int attemptOrder = 1;

            for (PracticeAnswer ans : questionAnswers) {
                attempts.add(toPracticeCycleDetail(ans, attemptOrder++));
            }

            // 질문 DTO 생성
            questionDetails.add(PracticeQuestionDetail.builder()
                    .questionId(questionId)
                    .questionOrder(qOrder++)
                    // 엔티티 필드명 확인 (questionText 또는 content)
                    .questionText(questionEntity.getQuestionText())
                    .attempts(attempts)
                    .build());
        }

        // 6. 최종 반환
        return PracticeHistoryDetailResponse.builder()
                .practiceId(practice.getPracticeId())

                // [추가됨] 토픽 ID 및 이름
                .topicId(practice.getTopic().getId())
                .topicTitle(practice.getTopic().getTopicName()) // getName() vs getTopicName() 확인

                // [추가됨] 유형 ID 및 코드
                .typeId(practice.getQuestionType().getId())
                .typeName(practice.getQuestionType().getTypeCode())

                .startedAt(practice.getStartedAt())
                .questions(questionDetails)
                .build();
    }

    // --- 내부 변환 메소드 ---
    private PracticeCycleDetail toPracticeCycleDetail(PracticeAnswer answer, int attemptOrder) {

        // 문장별 피드백 리스트 변환
        List<PracticeSentenceFeedbackResponse> sentenceFeedbacks = (answer.getFeedbacks() == null)
                ? Collections.emptyList()
                : answer.getFeedbacks().stream()
                .sorted(Comparator.comparingInt(PracticeSentenceFeedback::getSentenceOrder))
                .map(sf -> PracticeSentenceFeedbackResponse.builder()
                        .targetSentence(sf.getTargetSentence())
                        .targetSegment(sf.getTargetSegment())
                        .correctedSegment(sf.getImprovedSegment())
                        .comment(sf.getComment())
                        .build())
                .toList();

        return PracticeCycleDetail.builder()
                .attemptOrder(attemptOrder) // 1차 시도, 2차 시도...

                .userAnswer(site.okkul.be.domain.history.dto.PracticeAnswer.builder()
                        .koreanScript(answer.getKoreanScript())
                        .englishScript(answer.getEnglishScript())
                        .recordUrl(answer.getEnglishRecordUrl())
                        .build())

                .feedback(PracticeAiFeedback.builder()
                        .status(answer.getFeedbackStatus())
                        .improvedAnswer(answer.getImprovedAnswer())
                        .fluencyFeedback(answer.getFluencyFeedback())
                        .logicFeedback(answer.getLogicFeedback())
                        .relevanceFeedback(answer.getRelevanceFeedback())
                        .sentenceDetails(sentenceFeedbacks)
                        .build())
                .build();
    }
}
