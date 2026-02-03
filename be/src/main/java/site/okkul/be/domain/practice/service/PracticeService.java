package site.okkul.be.domain.practice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.auth.exception.AuthErrorCode;
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
import site.okkul.be.domain.practice.exception.PracticeErrorCode;
import site.okkul.be.domain.practice.mapper.PracticeMapper;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.exception.QuestionErrorCode;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.repository.QuestionRepository;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.exception.SurveyErrorCode;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicJpaRepository;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;
import site.okkul.be.global.config.SwaggerConfig;
import site.okkul.be.global.exception.BusinessException;

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
        User user = findUserById(userId);
        Survey survey = findSurveyByIdAndUserId(surveyId, userId);
        Topic topic = findTopicById(topicId);
        QuestionSet questionSet = findQuestionSetByType(typeId, survey, topicId);

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
        Practice practice = findPracticeByIdAndUserId(practiceId, userId);

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

    public Long createAnswerAndRequestFeedbackAsync(Long practiceId, PracticeFeedbackRequest request, MultipartFile audioFile, Long userId, boolean useRealAi) {
        Long practiceAnswerId = practiceAnswerCreator.createAndSaveAnswer(practiceId, request, audioFile);

        aiFeedbackTrigger.triggerAiFeedback(practiceAnswerId, useRealAi);

        return practiceAnswerId;
    }

    public PracticeAIFeedbackResult getFeedbackResult(Long practiceAnswerId, Long userId) {
        PracticeAnswer answer = findPracticeAnswerByIdAndUserId(practiceAnswerId, userId);

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

    // --- Private Helper Methods ---

    private User findUserById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_REGISTERED));
    }

    private Survey findSurveyByIdAndUserId(Long surveyId, Long userId) {
        return surveyJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new BusinessException(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    private Topic findTopicById(Long topicId) {
        return topicJpaRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.TOPIC_NOT_FOUND));
    }

    private QuestionSet findQuestionSetByType(Long typeId, Survey survey, Long topicId) {
        if (typeId.equals(QuestionType.INTRODUCE.getId())) {
            return questionSetRepository.findIntroQuestion(typeId)
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_SET_NOT_FOUND));
        }
        return questionSetRepository.findRandomByLevelAndTopicAndType(survey.getLevel(), topicId, typeId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_SET_NOT_FOUND));
    }

    private Practice findPracticeByIdAndUserId(Long practiceId, Long userId) {
        return practiceJpaRepository.findByPracticeIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new BusinessException(PracticeErrorCode.PRACTICE_NOT_FOUND));
    }

    private PracticeAnswer findPracticeAnswerByIdAndUserId(Long practiceAnswerId, Long userId) {
        return practiceAnswerRepository.findByPracticeAnswerIdAndPractice_User_Id(practiceAnswerId, userId)
                .orElseThrow(() -> new BusinessException(PracticeErrorCode.PRACTICE_ANSWER_NOT_FOUND));
    }
}
