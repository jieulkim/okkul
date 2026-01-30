package site.okkul.be.domain.practice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.PracticeAIFeedbackResult;
import site.okkul.be.domain.practice.dto.response.PracticeCreateResponse;
import site.okkul.be.domain.practice.dto.response.PracticeQuestionInfo;
import site.okkul.be.domain.practice.dto.response.PracticeQuestionResponse;
import site.okkul.be.domain.practice.dto.response.SentenceCorrection;
import site.okkul.be.domain.practice.entity.FeedbackStatus;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.entity.PracticeSentenceFeedback;
import site.okkul.be.domain.practice.mapper.PracticeMapper;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.repository.QuestionRepository;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicJpaRepository;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PracticeService {

    private final SurveyJpaRepository surveyJpaRepository;
    private final TopicJpaRepository topicJpaRepository;
    private final QuestionSetRepository questionSetRepository;
    private final PracticeJpaRepository practiceJpaRepository;
    private final PracticeAnswerJpaRepository practiceAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserJpaRepository userJpaRepository;
    private final PracticeMapper practiceMapper;
    private final PracticeAnswerCreator practiceAnswerCreator;
    private final AiFeedbackTrigger aiFeedbackTrigger;


    @Transactional
    public PracticeCreateResponse create(Long surveyId, Long topicId, Long typeId, Long userId) {
        Survey survey = surveyJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found or access denied"));
        Topic topic = topicJpaRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        QuestionSet questionSet = questionSetRepository.findRandomByLevelAndTopicAndType(survey.getLevel(), topicId, typeId)
                .orElseThrow(() -> new IllegalArgumentException("Question set is not exist"));
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Long> questionIds = questionSet.getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());
        Practice practice = Practice.builder()
                .topic(topic)
                .questionSet(questionSet)
                .questionType(questionSet.getQuestionType())
                .user(user)
                .questionIds(questionIds)
                .build();
        Practice saved = practiceJpaRepository.save(practice);
        return new PracticeCreateResponse(saved.getPracticeId(), saved.getStartedAt());
    }

    public PracticeQuestionResponse getPractice(Long practiceId, Long userId) {
        Practice practice = practiceJpaRepository.findByPracticeIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Pratice not found or access denied"));

        Set<Long> questionIds = practice.getQuestionIds();

        if (questionIds == null || questionIds.isEmpty()) {
            return PracticeQuestionResponse.builder()
                    .setId(practice.getQuestionSet() == null ? null : practice.getQuestionSet().getId())
                    .questions(Collections.emptyList())
                    .build();
        }

        List<Question> questions = questionRepository.findAllById(questionIds);
        List<PracticeQuestionInfo> practiceQuestions = questions.stream()
                .map(practiceMapper::toPracticeQuestionInfo)
                .collect(Collectors.toList());
        return PracticeQuestionResponse.builder()
                .setId(practice.getQuestionSet() == null ? null : practice.getQuestionSet().getId())
                .questions(practiceQuestions)
                .build();
    }

    public Long createAnswerAndRequestFeedbackAsync(Long practiceId, PracticeFeedbackRequest request, MultipartFile audioFile, Long userId) {
        Long practiceAnswerId = practiceAnswerCreator.createAndSaveAnswer(practiceId, request, audioFile);

        // 웹 스레드에서 헤더 값을 미리 boolean으로 추출
        boolean useRealAi = java.util.Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(httpRequest -> "true".equalsIgnoreCase(httpRequest.getHeader("X-Use-Real-AI")))
                .orElse(false);

        // 비동기 메소드에 원시 타입(boolean) 값을 전달
        aiFeedbackTrigger.triggerAiFeedback(practiceAnswerId, useRealAi);

        return practiceAnswerId;
    }

    public PracticeAIFeedbackResult getFeedbackResult(Long practiceAnswerId, Long userId) {
        PracticeAnswer answer = practiceAnswerRepository.findByPracticeAnswerIdAndPractice_User_Id(practiceAnswerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("PracticeAnswer not found or access denied"));

        if (answer.getFeedbackStatus() == FeedbackStatus.COMPLETED) {
            List<SentenceCorrection> sentenceFeedbacks = new ArrayList<>();
            List<PracticeSentenceFeedback> feedbacks = answer.getFeedbacks();
            for (PracticeSentenceFeedback feedback : feedbacks) {
                sentenceFeedbacks.add(practiceMapper.toSentenceCorrection(feedback));
            }

            return PracticeAIFeedbackResult.builder()
                    .feedbackStatus(answer.getFeedbackStatus())
                    .aiImprovedAnswer(answer.getImprovedAnswer())
                    .relevanceFeedback(answer.getRelevanceFeedback())
                    .logicFeedback(answer.getLogicFeedback())
                    .fluencyFeedback(answer.getFluencyFeedback())
                    .scriptCorrections(sentenceFeedbacks)
                    .build();
        } else {
            return new PracticeAIFeedbackResult(answer.getFeedbackStatus());
        }
    }
}
