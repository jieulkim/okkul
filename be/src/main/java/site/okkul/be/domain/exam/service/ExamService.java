package site.okkul.be.domain.exam.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.dto.ExamStartRequest;
import site.okkul.be.domain.exam.dto.ExamStartResponse;
import site.okkul.be.domain.exam.dto.QuestionResponse;
import site.okkul.be.domain.exam.entity.AnswerStatus;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpARepository;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;

import java.time.Instant;
import java.util.*;

/**
 * 모의고사 비지니스 로직 서비스
 *
 * 구조(표준 오픽에 가깝게):
 * - startExam(): 1~7번까지만 출제/저장
 * - getRemainingQuestions(): 8번~마지막 출제/저장 (7번 이후 난이도 조정 반영)
 *
 * Topic 다양성:
 * - 시험 시작 시 설문 Topic을 셔플해서 Exam.topicOrder에 저장
 * - 출제할 때마다 Exam.topicCursor로 다음 topic을 가져오고 +1
 * - 난이도 조정 후에도 같은 topic 흐름을 이어감
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamJpARepository examRepository;
    private final ExamAnswerJpaRepository examAnswerRepository;
    private final SurveyJpaRepository surveyRepository;
    private final QuestionSetRepository questionSetRepository;
//    private final MinioService minioService;
//    private final SttService sttService;
//    private final AiAnalysisService aiAnalysisService;

    // DB seed의 questiontype.type_code와 동일해야 함
    private static final String INTRO = "INTRODUCTION";
    private static final String COMBO2 = "COMBO2";
    private static final String COMBO3 = "COMBO3";
    private static final String RP1 = "RP1";
    private static final String RP2 = "RP2";
    private static final String RP3 = "RP3";
    private static final String AD2 = "AD2";

    /**
     * 1단계 출제: 1~7번만 생성/반환
     * - INTRODUCTION: topic/level 무관 랜덤 1개
     * - 나머지: level + (설문 topic) + type_code 기준으로 세트 랜덤
     */
    @Transactional
    public ExamStartResponse startExam(Long userId, ExamStartRequest request) {
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 데이터입니다."));

        int level = survey.getLevel();

        // 1) Exam 생성 (topicOrder/topicCursor 포함)
        Exam exam = examRepository.save(Exam.builder()
                .userId(userId)
                .surveyId(request.getSurveyId())
                .initialDifficulty(level)
                .createdAt(Instant.now())
//                .completed(false)
                .build());

        // 2) Topic 순서 셔플 후 Exam에 저장
        List<Long> shuffledTopicIds = createShuffledTopicOrder(survey);
        exam.initializeTopicOrder(shuffledTopicIds);

        // 3) 1~7번 Layout
        List<String> firstLayout = getFirstLayoutByLevel(level);

        // 4) 문제 조립
        List<Question> questions = new ArrayList<>();
        for (String typeCode : firstLayout) {
            if (INTRO.equals(typeCode)) {
                questions.addAll(getIntroQuestions());
            } else {
                Long topicId = nextTopicFromExam(exam);
                questions.addAll(getQuestions(level, topicId, typeCode));
            }
        }

        // 5) ExamAnswer 저장 (1번부터)
        return saveAnswersAndCreateResponse(exam, questions, 1);
    }

    /**
     * 2단계 출제: 8번~마지막 생성/반환
     * - 난이도 조정 후 adjustedDifficulty 기준으로 remaining layout을 조립
     * - topicCursor는 Exam에 저장되어 있어 startExam에서 사용한 topic 다음부터 이어짐
     */
    @Transactional
    public List<QuestionResponse> getRemainingQuestions(Long examId, int adjustedDifficulty) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 세션입니다. ID: " + examId));

        // 1) 난이도 업데이트
        exam.updateAdjustedDifficulty(adjustedDifficulty);

        // 2) 8번 이후가 이미 있으면 삭제 (중복 생성 방지)
        List<ExamAnswer> existingAnswers = examAnswerRepository.findAllByExamId(examId);
        List<ExamAnswer> answersToRemove = existingAnswers.stream()
//                .filter(a -> a.getQuestionOrder() >= 8)
                .toList();
        if (!answersToRemove.isEmpty()) {
            examAnswerRepository.deleteAll(answersToRemove);
        }

        // 3) 8번~마지막 Layout
        List<String> remainingLayout = getRemainingLayoutByLevel(adjustedDifficulty);

        // 4) 문제 조립
        List<Question> newQuestions = new ArrayList<>();
        for (String typeCode : remainingLayout) {
            Long topicId = nextTopicFromExam(exam);
            newQuestions.addAll(getQuestions(adjustedDifficulty, topicId, typeCode));
        }

        // 5) ExamAnswer 저장 (8번부터)
        ExamStartResponse resp = saveAnswersAndCreateResponse(exam, newQuestions, 8);
        return resp.getQuestions();
    }

    /**
     * 답변 제출
     * - answerId(=ExamAnswer PK)로 문항을 식별
     */
    @Transactional
    public void submitAnswer(Long examId, Long answerId, MultipartFile file) {
        ExamAnswer answer = examAnswerRepository.findById(answerId).orElseThrow();

//        if (!Objects.equals(answer.getExamId(), examId)) {
//            throw new IllegalArgumentException("examId와 answerId가 일치하지 않습니다.");
//        }

//        try {
//            String audioUrl = minioService.upload(file);
//            answer.uploadVoiceFile(audioUrl);
//
//            answer.startStt();
//            String script = sttService.convert(file);
//
//            answer.completeSttAndStartAnalysis(script);
//            aiAnalysisService.analyzeAndSaveFeedback(answerId, script);
//        } catch (Exception e) {
//            answer.markAsFailed();
//            throw new RuntimeException("답변 처리 중 오류가 발생했습니다.", e);
//        }
    }

    @Transactional
    public void completeExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("시험 세션을 찾을 수 없습니다."));
        exam.completeExam();
    }

    /**
     * 1~7번 구성
     * - Lv1~2: INTRO(1) + COMBO2 * 3세트(=6문항) => 총 7문항
     * - Lv3~6: INTRO(1) + COMBO3 * 2세트(=6문항) => 총 7문항
     */
    private List<String> getFirstLayoutByLevel(int level) {
        if (level >= 3) {
            return List.of(INTRO, COMBO3, COMBO3);
        }
        return List.of(INTRO, COMBO2, COMBO2, COMBO2);
    }

    /**
     * 8번~마지막 구성
     * - Lv1~2(총 12문항): COMBO2(1세트=2문항) + RP1(1세트=1문항) + RP2(1세트=2문항) => 5문항 (8~12)
     * - Lv3~4(총 15문항): COMBO3(1세트=3문항) + RP3(1세트=3문항) + RP2(1세트=2문항) => 8문항 (8~15)
     * - Lv5~6(총 15문항): COMBO3(1세트=3문항) + RP3(1세트=3문항) + AD2(1세트=2문항) => 8문항 (8~15)
     */
    private List<String> getRemainingLayoutByLevel(int level) {
        if (level >= 5) {
            return List.of(COMBO3, RP3, AD2);
        } else if (level >= 3) {
            return List.of(COMBO3, RP3, RP2);
        }
        return List.of(COMBO2, RP1, RP2);
    }

    /**
     * 설문에서 선택된 topicId 목록을 가져와 셔플한 뒤 반환
     */
    private List<Long> createShuffledTopicOrder(Survey survey) {
        List<Long> topicIds = new ArrayList<>(survey.getTopicIds());

        if (topicIds.isEmpty()) {
            throw new IllegalStateException("설문에서 선택된 주제가 없습니다. 최소 한 개 이상의 주제가 필요합니다.");
        }

        Collections.shuffle(topicIds);
        return topicIds;
    }

    /**
     * Exam의 topicOrder/topicCursor를 이용해 다음 topicId를 가져오고 cursor를 증가
     * - topicOrder는 Exam 내부에서 파싱
     * - topicCursor 증가는 Exam이 책임
     */
    private Long nextTopicFromExam(Exam exam) {
        Long topicId = exam.getCurrentTopicId();
        exam.incrementTopicCursor();
        return topicId;
    }

    /**
     * INTRODUCTION 세트는 topic/level과 무관하게 랜덤 1개를 뽑음
     * - 세트 내부 질문은 order 오름차순 정렬 후 반환
     */
    private List<Question> getIntroQuestions() {
        QuestionSet set = questionSetRepository.findRandomByTypeCode(INTRO)
                .orElseThrow(() -> new EntityNotFoundException("INTRODUCTION 세트가 없습니다."));

        List<Question> questions = new ArrayList<>(set.getQuestions());
        questions.sort(Comparator.comparingInt(Question::getOrder));
        return questions;
    }

    /**
     * level + topicId + typeCode로 QuestionSet을 1개 랜덤 조회 후, 세트에 속한 질문을 반환
     * - 기대 문항 수(question_cnt) 및 실제 질문 수 검증
     * - 질문은 order 기준 정렬
     */
    private List<Question> getQuestions(Integer level, Long topicId, String typeCode) {
        QuestionSet set = questionSetRepository.findRandomByLevelAndTopicIdAndTypeCode(level, topicId, typeCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("조건에 맞는 세트가 없습니다. (Lv:%d, Topic:%d, Type:%s)", level, topicId, typeCode)));

        int expected = expectedCountByTypeCode(typeCode);

        // DB에 question_cnt를 관리한다면 검증(선택)
        if (set.getQuestionCnt() == null || set.getQuestionCnt() != expected) {
            throw new IllegalStateException("question_cnt 불일치 setId=" + set.getId()
                    + " expected=" + expected + " actual=" + set.getQuestionCnt());
        }

        List<Question> questions = new ArrayList<>(set.getQuestions());
        questions.sort(Comparator.comparingInt(Question::getOrder));

        if (questions.size() != expected) {
            throw new IllegalStateException("세트 문항 개수 불일치 setId=" + set.getId()
                    + " expected=" + expected + " actual=" + questions.size());
        }

        return questions;
    }

    /**
     * typeCode에 따른 세트 기대 문항 수 반환
     */
    private int expectedCountByTypeCode(String typeCode) {
        return switch (typeCode) {
            case INTRO, RP1 -> 1;
            case COMBO2, RP2, AD2 -> 2;
            case COMBO3, RP3 -> 3;
            default -> throw new IllegalArgumentException("Unknown typeCode: " + typeCode);
        };
    }

    /* ==================================================
       Save answers helper
       ================================================== */

    /**
     * Question 리스트를 ExamAnswer로 저장하고 응답 DTO로 변환
     *
     * @param startOrder questionOrder 시작 번호 (1 또는 8)
     */
    private ExamStartResponse saveAnswersAndCreateResponse(Exam exam, List<Question> questions, int startOrder) {
        List<ExamAnswer> answerEntities = new ArrayList<>();
        int order = startOrder;

        for (Question q : questions) {
            answerEntities.add(ExamAnswer.builder()
//                    .examId(exam.getId())
//                    .question(q)
//                    .questionOrder(order++)
                    .status(AnswerStatus.READY)
                    .build());
        }

        List<ExamAnswer> saved = examAnswerRepository.saveAll(answerEntities);

        List<QuestionResponse> dtos = saved.stream()
                .map(a -> QuestionResponse.builder()
//                        .answerId(a.getId())
//                        .questionOrder(a.getQuestionOrder())
//                        .questionText(a.getQuestion().getQuestionText())
//                        .audioUrl(a.getQuestion().getAudioUrl())
                        .build())
                .toList();

        return ExamStartResponse.builder()
                .examId(exam.getId())
                .totalQuestions(dtos.size())
                .questions(dtos)
                .build();
    }
}
