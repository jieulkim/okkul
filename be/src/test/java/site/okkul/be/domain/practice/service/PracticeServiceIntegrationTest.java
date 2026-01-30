package site.okkul.be.domain.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.PracticeCreateResponse;
import site.okkul.be.domain.practice.entity.FeedbackStatus;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.repository.QuestionRepository;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.qustion.repository.QuestionTypeRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicJpaRepository;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;
import site.okkul.be.infra.ai.AiClient;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;
import site.okkul.be.infra.storage.FileStorageService;


import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@Tag("integration")
@SpringBootTest
class PracticeServiceIntegrationTest {

    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PracticeJpaRepository practiceJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private SurveyJpaRepository surveyJpaRepository;
    @Autowired
    private TopicJpaRepository topicJpaRepository;
    @Autowired
    private QuestionTypeRepository questionTypeRepository;
    @Autowired
    private QuestionSetRepository questionSetRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private PracticeAnswerJpaRepository practiceAnswerRepository;

    @MockitoBean
    private AiClient aiClient;
    @MockitoBean
    private FileStorageService fileStorageService;


    @AfterEach
    void cleanup() {
        // 외래 키 제약 조건을 피하기 위해 자식 테이블부터 삭제
        practiceAnswerRepository.deleteAllInBatch();
        practiceJpaRepository.deleteAllInBatch();
        questionRepository.deleteAllInBatch();
        questionSetRepository.deleteAllInBatch();
        surveyJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }


    @DisplayName("유형별 연습을 생성하고, 관련된 문제 ID들이 올바르게 저장된다.")
    @Test
    void createPractice_success() {
        // given: 테스트를 위한 더미 데이터 생성 및 저장
        User user = userJpaRepository.save(User.builder().email("test@okkul.site").provider(OAuthProvider.GOOGLE).providerId("ABC").build());
        Topic topic = topicJpaRepository.findById(101L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 토픽입니다."));
        QuestionType questionType = questionTypeRepository.findById(1L).orElseGet(() -> questionTypeRepository.save(QuestionType.builder().typeCode("RP2").description("기본 질문").build()));

        Survey survey = surveyJpaRepository.save(Survey.builder().userId(user.getId()).level(3).build());

        Question question1 = Question.builder().questionText("Q1. 인공지능이란?").audioUrl("q1.mp3").order(1).build();
        Question question2 = Question.builder().questionText("Q2. 머신러닝이란?").audioUrl("q2.mp3").order(2).build();

        QuestionSet questionSet = QuestionSet.builder()
                .level(3)
                .topic(topic)
                .questionType(questionType)
                .questions(List.of(question1, question2))
                .build();

        question1.assignTo(questionSet);
        question2.assignTo(questionSet);
        questionSetRepository.save(questionSet);


        // when: 서비스 메서드 호출
        PracticeCreateResponse response = practiceService.create(survey.getSurveyId(), topic.getId(), questionType.getId(), user.getId());


        // then: 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getPracticeId()).isNotNull();

        Practice foundPractice = practiceJpaRepository.findByIdAndUserIdWithQuestionIds(response.getPracticeId(), user.getId())
                .orElseThrow(() -> new AssertionError("Practice should be created"));

        assertThat(foundPractice.getUser().getId()).isEqualTo(user.getId());
        assertThat(foundPractice.getTopic().getId()).isEqualTo(topic.getId());
        assertThat(foundPractice.getQuestionSet().getId()).isEqualTo(questionSet.getId());
        assertThat(foundPractice.getQuestionIds()).hasSize(2);
        assertThat(foundPractice.getQuestionIds()).containsExactlyInAnyOrder(
                question1.getId(),
                question2.getId()
        );
    }

    @Nested
    @DisplayName("createAnswerAndRequestFeedbackAsync 메서드는")
    class CreateAnswerAndRequestFeedbackAsyncTest {

        private User user;
        private Practice practice;
        private Question question;

        @BeforeEach
        void setup() {
            user = userJpaRepository.save(User.builder().email("user@test.com").provider(OAuthProvider.GOOGLE).providerId("123").build());
            Topic topic = topicJpaRepository.findById(101L).orElseThrow();
            QuestionType questionType = questionTypeRepository.findById(5L).orElseThrow();
            QuestionSet questionSet = questionSetRepository.save(QuestionSet.builder().level(5).topic(topic).questionType(questionType).build());
            question = questionRepository.save(Question.builder().questionText("Test Question").audioUrl("test.mp3").questionSet(questionSet).build());
            practice = practiceJpaRepository.save(Practice.builder().user(user).topic(topic).questionSet(questionSet).questionType(questionType).build());
        }

        @Test
        @DisplayName("성공 - AI 피드백 요청 후 상태가 COMPLETED로 변경된다")
        void success_changesStatusToCompleted() {
            // given
            var request = new PracticeFeedbackRequest(question.getId(), "korean", "english");
            var audioFile = new MockMultipartFile("audio", "test.mp3", "audio/mpeg", "data".getBytes());

            given(fileStorageService.upload(any(), any())).willReturn("http://storage.com/test.mp3");

            var aiResponse = AiFeedbackResponse.builder()
                .relevance_feedback("Good relevance")
                .logic_feedback("Good logic")
                .fluency_feedback("Good fluency")
                .improved_answer("Improved answer")
                .build();
            given(aiClient.requestFeedback(any())).willReturn(aiResponse);

            // when
            Long practiceAnswerId = practiceService.createAnswerAndRequestFeedbackAsync(practice.getPracticeId(), request, audioFile, user.getId());
            log.info("practiceAnswerId: {}", practiceAnswerId);
            // then
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                        .orElseThrow(() -> new AssertionError("PracticeAnswer should exist"));
                
                assertThat(answer.getFeedbackStatus()).isEqualTo(FeedbackStatus.COMPLETED);
                assertThat(answer.getRelevanceFeedback()).isEqualTo("Good relevance");
                assertThat(answer.getImprovedAnswer()).isEqualTo("Improved answer");
            });
        }

        @Test
        @DisplayName("실패 - AI 피드백 요청 실패 시 상태가 FAILED로 변경된다")
        void failure_changesStatusToFailed() {
            // given
            var request = new PracticeFeedbackRequest(question.getId(), "korean", "english");
            var audioFile = new MockMultipartFile("audio", "test.mp3", "audio/mpeg", "data".getBytes());

            given(fileStorageService.upload(any(), any())).willReturn("http://storage.com/test.mp3");
            given(aiClient.requestFeedback(any())).willThrow(new RuntimeException("AI Server Error"));

            // when
            Long practiceAnswerId = practiceService.createAnswerAndRequestFeedbackAsync(practice.getPracticeId(), request, audioFile, user.getId());

            // then
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                PracticeAnswer answer = practiceAnswerRepository.findById(practiceAnswerId)
                        .orElseThrow(() -> new AssertionError("PracticeAnswer should exist"));

                assertThat(answer.getFeedbackStatus()).isEqualTo(FeedbackStatus.FAILED);
            });
        }
    }
}
