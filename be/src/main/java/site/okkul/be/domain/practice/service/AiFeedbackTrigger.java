package site.okkul.be.domain.practice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.practice.entity.FeedbackStatus;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.entity.PracticeSentenceFeedback;
import site.okkul.be.domain.practice.mapper.PracticeMapper;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.infra.ai.AiClient;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackTrigger {

    private final PracticeAnswerJpaRepository practiceAnswerRepository;
    private final AiClient aiClient;
    private final PracticeMapper practiceMapper;

    private AiFeedbackTrigger self;

    @Autowired
    public void setSelf(@Lazy AiFeedbackTrigger self) {
        this.self = self;
    }

    /**
     * AI 피드백 요청을 비동기적으로 처리하는 메소드
     */
    @Async
    @Transactional
    public void triggerAiFeedback(Long practiceAnswerId) {
        log.info("비동기 피드백 처리 시작. PracticeAnswer ID: {}", practiceAnswerId);

        // Step 1: 상태 변경을 즉시 커밋
        self.updateStatusToProcessing(practiceAnswerId);

        // Step 2: 새 트랜잭션에서 데이터를 다시 로드하여 지연로딩 문제 해결
        PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                .orElseThrow(() -> new IllegalStateException("PracticeAnswer disappeared after status update"));

        AiFeedbackRequest aiRequest = AiFeedbackRequest.builder()
                .question_text(answer.getQuestion().getQuestionText())
                .user_answer(answer.getEnglishScript())
                .user_korean_script(answer.getKoreanScript())
                .build();

        try {
            log.info("AI Request Body: {}", new ObjectMapper().writeValueAsString(aiRequest));
        } catch (Exception e) {
            log.error("Failed to serialize AiRequest", e);
        }

        log.info("AI 서버에 피드백 요청 전송. PracticeAnswer ID: {}", practiceAnswerId);
        try {
            AiFeedbackResponse aiResponse = aiClient.requestFeedback(aiRequest);
            self.handleAiSuccess(practiceAnswerId, aiResponse);
        } catch (Exception e) {
            log.error("AI 요청 처리 실패 (동기). DB 상태 변경. PracticeAnswer ID: {}", practiceAnswerId, e);
            self.handleAiFailure(practiceAnswerId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusToProcessing(Long practiceAnswerId) {
        PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                .orElseThrow(() -> new IllegalArgumentException("PracticeAnswer not found"));
        answer.updateStatus(FeedbackStatus.PROCESSING);
        log.info("상태를 PROCESSING으로 변경. PracticeAnswer ID: {}", practiceAnswerId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAiSuccess(Long practiceAnswerId, AiFeedbackResponse aiResponse) {
        log.info("AI 응답 성공. DB 업데이트 시작. PracticeAnswer ID: {}", practiceAnswerId);
        try {
            PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                    .orElseThrow(() -> new IllegalArgumentException("PracticeAnswer not found"));
            // 피드백 적용
            applyAiFeedbackToAnswer(answer, aiResponse);
        } catch (Exception e) {
            log.error("AI 응답 성공 후 DB 업데이트 중 예외 발생. PracticeAnswer ID: {}", practiceAnswerId, e);
            self.handleAiFailure(practiceAnswerId, e);
        }
    }

    /**
     * AI 피드백 DTO를 PracticeAnswer 엔티티에 적용하는 로직을 담당합니다.
     * 이 메서드는 AiFeedbackTrigger 내에서만 사용됩니다.
     */
    private void applyAiFeedbackToAnswer(PracticeAnswer answer, AiFeedbackResponse aiResponse) {
        List<PracticeSentenceFeedback> newFeedbacks = practiceMapper.toSentenceFeedbacks(aiResponse.getSentence_details(), answer);
        answer.applyFeedback(
                aiResponse.getImproved_answer(),
                aiResponse.getRelevance_feedback(),
                aiResponse.getLogic_feedback(),
                aiResponse.getFluency_feedback(),
                newFeedbacks
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAiFailure(Long practiceAnswerId, Throwable error) {
        log.error("AI 요청 처리 실패. DB 상태 변경. PracticeAnswer ID: {}", practiceAnswerId, error);
        try {
            PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                    .orElseThrow(() -> new IllegalArgumentException("PracticeAnswer not found"));
            answer.updateStatus(FeedbackStatus.FAILED);
        } catch (Exception e) {
            log.error("AI 실패 처리 중 DB 업데이트 실패. PracticeAnswer ID: {}", practiceAnswerId, e);
        }
    }
}
