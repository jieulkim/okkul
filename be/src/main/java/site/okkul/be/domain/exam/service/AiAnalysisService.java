package site.okkul.be.domain.exam.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamReportJpaRepository;
//import site.okkul.be.domain.exam.repository.TypeFeedbackJpaRepository;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final ExamAnswerJpaRepository examAnswerRepository;
//    private final TypeFeedbackJpaRepository typeFeedbackRepository;
    private final ExamReportJpaRepository examReportRepository;
//    private final AiApiClient aiApiClient;

    @Async
    @Transactional
    public void analyzeAndSaveFeedback(Long answerId, String script) {
//        ExamAnswer examAnswer = examAnswerRepository.findById(answerId)
//                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));

//        Long examId = examAnswer.getExamId();

//        try {
//            // 1. AI 분석 및 저장
//            ApiResponse aiResponse = aiApiClient.fetchAnalysis(script);
//
//            TypeFeedback feedback = TypeFeedback.createFeedback(
//                    examAnswer,
//                    aiResponse.getGScore(), aiResponse.getGFeedback(),
//                    aiResponse.getVScore(), aiResponse.getVFeedback(),
//                    aiResponse.getLScore(), aiResponse.getLFeedback(),
//                    aiResponse.getFScore(), aiResponse.getFFeedback(),
//                    aiResponse.getEnhancedText()
//            );
//
//            // 중요: 15번 리포트 생성 시 조회가 가능하도록 flush 수행
//            typeFeedbackRepository.saveAndFlush(feedback);
//
//            // 2. 마지막 문항(15번)인 경우 리포트 생성
//            if (examAnswer.getQuestionOrder() == 15) {
//                generateFinalReport(examId);
//            }
//
//            examAnswer.finalizeAnalysis();
//        } catch (Exception e) {
//            examAnswer.markAsFailed();
//        }
    }
//
//    private void generateFinalReport(Long examId) {
//        List<TypeFeedback> feedbacks = typeFeedbackRepository.findAllByExamIdWithAnswerAndQuestion(examId);
//
//        if (feedbacks.isEmpty()) {
//            throw new IllegalStateException("시험 피드백이 존재하지 않습니다.");
//        }
//
//        // 영역별 평균 계산
//        double avgG = feedbacks.stream().mapToInt(TypeFeedback::getGrammarScore).average().orElse(0.0);
//        double avgV = feedbacks.stream().mapToInt(TypeFeedback::getVocabScore).average().orElse(0.0);
//        double avgL = feedbacks.stream().mapToInt(TypeFeedback::getLogicScore).average().orElse(0.0);
//        double avgF = feedbacks.stream().mapToInt(TypeFeedback::getFluencyScore).average().orElse(0.0);
//
//        // 유형별 점수 집계 - 이미 fetch join으로 가져온 데이터 사용
//        Map<String, List<Double>> typeScoresMap = new HashMap<>();
//
//        for (TypeFeedback fb : feedbacks) {
//            double questionTotal = (fb.getGrammarScore() + fb.getVocabScore() +
//                    fb.getLogicScore() + fb.getFluencyScore()) / 4.0;
//
//            String qType = fb.getExamAnswer().getQuestion()
//                    .getQuestionSet()
//                    .getQuestionType()
//                    .getTypeCode(); // "Intro", "Combo2" 등
//
//            typeScoresMap.computeIfAbsent(qType, k -> new ArrayList<>()).add(questionTotal);
//        }
//
//        // 유형별 평균 계산
//        if (typeScoresMap.isEmpty()) {
//            throw new IllegalStateException("유형별 점수 집계 실패");
//        }
//
//        Map<String, Double> typeAvgMap = new HashMap<>();
//        typeScoresMap.forEach((type, scores) ->
//                typeAvgMap.put(type, scores.stream().mapToDouble(d -> d).average().orElse(0.0))
//        );
//
//        String strengthType = Collections.max(typeAvgMap.entrySet(), Map.Entry.comparingByValue()).getKey();
//        String weaknessType = Collections.min(typeAvgMap.entrySet(), Map.Entry.comparingByValue()).getKey();
//
//        double totalScore = (avgG + avgV + avgL + avgF) / 4.0;
//
//        // 리포트 생성 및 저장
//        ExamReport report = ExamReport.createReport(
//                examId, avgG, avgV, avgL, avgF, totalScore,
//                calculateGrade(totalScore), strengthType, weaknessType,
//                generateOverallComment(totalScore, strengthType, weaknessType)
//        );
//        examReportRepository.save(report);
//    }

    private String calculateGrade(double score) {
        if (score >= 90) return "AL";
        if (score >= 80) return "IH";
        if (score >= 70) return "IM3";
        if (score >= 60) return "IM2";
        if (score >= 50) return "IM1";
        return "IL";
    }

    private String generateOverallComment(double score, String s, String w) {
        return String.format("총점 %.1f점으로 우수한 성적을 거두셨습니다. 특히 %s 유형에서 강점을 보였으나 %s 유형은 보완이 필요합니다.", score, s, w);
    }
}
