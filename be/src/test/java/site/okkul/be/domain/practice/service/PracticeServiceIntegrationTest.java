package site.okkul.be.domain.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.dto.response.PracticeCreateResponse;
import site.okkul.be.domain.practice.entity.FeedbackStatus;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.practice.service.PracticeService;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.exception.QuestionErrorCode;
import site.okkul.be.domain.question.repository.QuestionRepository;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.entity.TopicCategory;
import site.okkul.be.domain.topic.repository.TopicCategoryRepository;
import site.okkul.be.domain.topic.repository.TopicJpaRepository;
import site.okkul.be.domain.user.entity.OAuthProvider;
import site.okkul.be.domain.user.entity.User;
import site.okkul.be.domain.user.repository.UserJpaRepository;
import site.okkul.be.global.exception.BusinessException;
import site.okkul.be.infra.ai.AiClient;
import site.okkul.be.infra.ai.AiClientProvider;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;
import site.okkul.be.infra.storage.FileStorageService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
    private TopicCategoryRepository topicCategoryRepository;
    @Autowired
    private QuestionSetRepository questionSetRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private PracticeAnswerJpaRepository practiceAnswerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AiClientProvider aiClientProvider;
    @MockitoBean
    private FileStorageService fileStorageService;

    private User user;
    private Practice practice;
    private TopicCategory topicCategory;
    private Topic topic;
    private Survey survey;
    private Question question1;
    private Question question2;
    private QuestionSet questionSet;
    private AiClient mockAiClient;
    private List<String> tableNames;

    @BeforeEach
    void setUp() {
        // 테이블 이름 캐싱 (한 번만 실행)
        if (tableNames == null) {
            tableNames = jdbcTemplate.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                            (rs, rowNum) -> rs.getString(1))
                    .stream()
                    .filter(tableName -> !tableName.equals("flyway_schema_history"))
                    .collect(Collectors.toList());
        }

        // 테스트 데이터 생성
        user = userJpaRepository.save(User.builder().email("test@okkul.site").provider(OAuthProvider.GOOGLE).providerId("ABC").build());
        topicCategory = topicCategoryRepository.save(TopicCategory.builder().id(1L).categoryName("categoryName").categoryCode("categoryCode").build());
        topic = topicJpaRepository.save(Topic.builder().id(101L).topicName("Topic1").topicCode("TopicCode").category(topicCategory).build());
        survey = surveyJpaRepository.save(Survey.builder().userId(user.getId()).level(3).build());
        question1 = Question.builder().questionText("Q1. 인공지능이란?").audioUrl("q1.mp3").order(1).build();
        question2 = Question.builder().questionText("Q2. 머신러닝이란?").audioUrl("q2.mp3").order(2).build();

        questionSet = QuestionSet.builder()
                .level(3)
                .topic(topic)
                .questionType(QuestionType.COMBO2)
                .questions(List.of(question1, question2))
                .build();

        question1.assignTo(questionSet);
        question2.assignTo(questionSet);
        questionSetRepository.save(questionSet);

        practice = practiceJpaRepository.save(Practice.builder().user(user).topic(topic).questionSet(questionSet).questionType(QuestionType.COMBO2).build());
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("SET session_replication_role = 'replica';");
        tableNames.forEach(tableName -> jdbcTemplate.execute("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE;"));
        jdbcTemplate.execute("SET session_replication_role = 'origin';");
    }

    @DisplayName("유형별 연습을 생성하고, 관련된 문제 ID들이 올바르게 저장된다.")
    @Test
    void createPractice_success() {
        // given (data is prepared in setUp)

        // when: 서비스 메서드 호출
        PracticeCreateResponse response = practiceService.create(
                survey.getSurveyId(),
                topic.getId(),
                questionSet.getQuestionType().getId(),
                user.getId());

        // then: 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getPracticeId()).isNotNull();

        Practice foundPractice = practiceJpaRepository.findById(response.getPracticeId())
                .orElseThrow(() -> new AssertionError("Practice should be created"));

        assertThat(foundPractice.getUser().getId()).isEqualTo(user.getId());
        assertThat(foundPractice.getTopic().getId()).isEqualTo(topic.getId());
        assertThat(foundPractice.getQuestionSet().getId()).isEqualTo(questionSet.getId());
    }

    @Nested
    @DisplayName("createAnswerAndRequestFeedbackAsync 메서드는")
    class CreateAnswerAndRequestFeedbackAsyncTest {

        @BeforeEach
        void setup() {
            mockAiClient = mock(AiClient.class);
            given(aiClientProvider.getClient(anyBoolean())).willReturn(mockAiClient);
        }

        @Test
        @DisplayName("성공 - AI 피드백 요청 후 상태가 COMPLETED로 변경된다")
        void success_changesStatusToCompleted() {
            // given
            var request = new PracticeFeedbackRequest(question1.getId(), "korean", "english");
            var audioFile = new MockMultipartFile("audio", "test.mp3", "audio/mpeg", "data".getBytes());

            given(fileStorageService.upload(any(), any())).willReturn("http://storage.com/test.mp3");

            var aiResponse = AiFeedbackResponse.builder()
                    .relevance_feedback("Good relevance")
                    .logic_feedback("Good logic")
                    .fluency_feedback("Good fluency")
                    .improved_answer("Improved answer")
                    .build();
            given(mockAiClient.requestFeedback(any())).willReturn(aiResponse);

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
            var request = new PracticeFeedbackRequest(question1.getId(), "korean", "english");
            var audioFile = new MockMultipartFile("audio", "test.mp3", "audio/mpeg", "data".getBytes());

            given(fileStorageService.upload(any(), any())).willReturn("http://storage.com/test.mp3");
            given(mockAiClient.requestFeedback(any())).willThrow(new RuntimeException("AI Server Error"));

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
