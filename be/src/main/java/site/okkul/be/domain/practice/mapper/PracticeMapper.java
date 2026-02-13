package site.okkul.be.domain.practice.mapper;

import site.okkul.be.domain.history.dto.PracticeHistorySummary;
import org.springframework.stereotype.Component;
import site.okkul.be.domain.practice.dto.response.PracticeQuestionInfo;
import site.okkul.be.domain.practice.dto.response.SentenceCorrection;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.entity.PracticeSentenceFeedback;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.infra.ai.dto.SentenceFeedbackResponse;

import java.time.Instant;
import java.util.*;

@Component
public class PracticeMapper {

    /**
     * Topic, QuestionSet, QuestionType, User, PracticeQuestion (Entity) -> Practice (Entity)
     * @param topic
     * @param questionSet
     * @param questionType
     * @param user
     * @param questionIds
     * @return
     */
    public Practice toEntity(Topic topic, QuestionSet questionSet, QuestionType questionType, User user, Set<Long> questionIds) {
        return Practice.builder()
                .startedAt(Instant.now())
                .user(user)
                .questionType(questionType)
                .topic(topic)
                .questionSet(questionSet)
                .questionIds(questionIds)
                .build();
    }

    /**
     * Question (Entity) -> PracticeQuestionInfo (Dto)
     * @param question
     * @return
     */
    public PracticeQuestionInfo toPracticeQuestionInfo(Question question) {
        return PracticeQuestionInfo.builder()
                .questionId(question.getId())
                .questionOrder(question.getOrder())
                .questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .build();
    }

    /**
     * PracticeSentenceFeedback (Entity) -> SentenceCorrection (Dto)
     * @param feedback
     * @return
     */
    public SentenceCorrection toSentenceCorrection(PracticeSentenceFeedback feedback) {
        return SentenceCorrection.builder()
                .targetSentence(feedback.getTargetSentence())
                .originalSegment(feedback.getTargetSegment())
                .correctedSegment(feedback.getImprovedSegment())
                .sentenceOrder(feedback.getSentenceOrder())
                .comment(feedback.getComment())
                .build();
    }

    /**
     * List<SentenceFeedbackResponse> (Dto), PracticeAnswer (Entity) -> List<PracticeSentenceFeedback> (Entity)
     * @param sentenceFeedbackResponses
     * @param practiceAnswer
     * @return
     */
    public List<PracticeSentenceFeedback> toSentenceFeedbacks(List<SentenceFeedbackResponse> sentenceFeedbackResponses, PracticeAnswer practiceAnswer) {
        if (sentenceFeedbackResponses == null) {
            return Collections.emptyList();
        }
        return sentenceFeedbackResponses.stream()
                .map(dto -> {
                    PracticeSentenceFeedback feedback = PracticeSentenceFeedback.builder()
                            .targetSentence(dto.getTarget_sentence())
                            .targetSegment(dto.getTarget_text())
                            .improvedSegment(dto.getImproved_text())
                            .comment(dto.getFeedback())
                            .sentenceOrder(dto.getSentence_order())
                            .build();
                    feedback.setPracticeAnswer(practiceAnswer); // 양방향 연관관계 설정
                    return feedback;
                })
                .toList();
    }

    /**
     * Practice (Entity) -> PracticeSummaryResponse (Dto)
     * @param practice
     * @return
     */
    public PracticeHistorySummary toPracticeSummaryResponse(Practice practice) {
        return PracticeHistorySummary.builder()
                .practiceId(practice.getPracticeId())
                .startedAt(practice.getStartedAt())
                .topicId(practice.getTopic().getId())
                .topic(practice.getTopic().getTopicName())
                .typeId(practice.getQuestionType().getId())
                .typeName(practice.getQuestionType().getTypeCode())
                .build();
    }
}
