package site.okkul.be.domain.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.question.dto.QuestionRequest;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.repository.QuestionRepository;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.entity.TopicCategory;
import site.okkul.be.domain.topic.repository.TopicCategoryRepository;
import site.okkul.be.domain.topic.repository.TopicRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private QuestionSetRepository questionSetRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private TopicCategoryRepository topicCategoryRepository;

	private Long savedSetId;
	private Long savedQuestionId;

	@BeforeEach
	void setUp() {
		// 1. TopicCategory 생성 및 저장
		TopicCategory category = TopicCategory.builder()
				.id(999L)
				.categoryCode("TEST_CATEGORY")
				.categoryName("테스트 카테고리")
				.build();
		topicCategoryRepository.save(category);

		// 2. Topic 생성 및 저장
		Topic topic = Topic.builder()
				.id(999L)
				.topicCode("TEST_TOPIC")
				.topicName("테스트 토픽")
				.category(category)
				.build();
		topicRepository.save(topic);

		// 3. QuestionSet 생성 및 저장
		QuestionSet questionSet = QuestionSet.builder()
				.level(1)
				.questionCnt(0)
				.topic(topic)
				.questionType(QuestionType.INTRODUCE)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
		QuestionSet savedSet = questionSetRepository.save(questionSet);
		this.savedSetId = savedSet.getId();

		// 4. Question 생성 및 저장
		Question question = Question.builder()
				.questionText("Old Question")
				.audioUrl("http://old.url")
				.order(1)
				.questionSet(savedSet)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
		Question savedQuestion = questionRepository.save(question);
		this.savedQuestionId = savedQuestion.getId();
	}

	@Nested
	@DisplayName("POST /question-sets/{setId}/questions")
	class AddQuestion {
		@Test
		@DisplayName("201 Created - 질문 추가 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			QuestionRequest request = new QuestionRequest("New Question", "http://example.com/new.mp3", 2);

			mockMvc.perform(post("/question-sets/{setId}/questions", savedSetId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.questionText").value("New Question"));
		}
	}

	@Nested
	@DisplayName("PATCH /question-sets/{setId}/questions/{questionId}")
	class UpdateQuestion {
		@Test
		@DisplayName("200 OK - 질문 수정 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			QuestionRequest request = new QuestionRequest("Updated Question", "http://example.com/updated.mp3", 1);

			mockMvc.perform(patch("/question-sets/{setId}/questions/{questionId}", savedSetId, savedQuestionId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.questionText").value("Updated Question"));
		}
	}

	@Nested
	@DisplayName("DELETE /question-sets/{setId}/questions/{questionId}")
	class DeleteQuestion {
		@Test
		@DisplayName("204 No Content - 질문 삭제 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			mockMvc.perform(delete("/question-sets/{setId}/questions/{questionId}", savedSetId, savedQuestionId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNoContent());
		}
	}
}
