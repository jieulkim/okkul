package site.okkul.be.domain.qustion.controller;

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
import site.okkul.be.domain.qustion.dto.QuestionSetRequest;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.entity.TopicCategory;
import site.okkul.be.domain.topic.repository.TopicCategoryRepository;
import site.okkul.be.domain.topic.repository.TopicRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionSetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private QuestionSetRepository questionSetRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private TopicCategoryRepository topicCategoryRepository;

	private Long savedSetId;
	private Long topicId1;
	private Long topicId2;

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
		Topic topic1 = Topic.builder().id(998L).topicCode("TEST_TOPIC_1").topicName("테스트 토픽 1").category(category).build();
		Topic topic2 = Topic.builder().id(999L).topicCode("TEST_TOPIC_2").topicName("테스트 토픽 2").category(category).build();
		topicRepository.save(topic1);
		topicRepository.save(topic2);
		this.topicId1 = topic1.getId();
		this.topicId2 = topic2.getId();

		// 3. QuestionSet 생성 및 저장
		QuestionSet questionSet = QuestionSet.builder()
				.level(1)
				.topic(topic1)
				.questionType(QuestionType.INTRODUCE)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
		QuestionSet savedSet = questionSetRepository.save(questionSet);
		this.savedSetId = savedSet.getId();
	}

	@Nested
	@DisplayName("GET /question-sets")
	class GetQuestionSets {
		@Test
		@DisplayName("200 OK - 질문 세트 목록 조회 성공")
		@WithMockUser
		void success() throws Exception {
			mockMvc.perform(get("/question-sets")
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray());
		}
	}

	@Nested
	@DisplayName("GET /question-sets/{setId}")
	class GetQuestionSet {
		@Test
		@DisplayName("200 OK - 질문 세트 단건 조회 성공")
		@WithMockUser
		void success() throws Exception {
			mockMvc.perform(get("/question-sets/{setId}", savedSetId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.setId").value(savedSetId));
		}
	}

	@Nested
	@DisplayName("POST /question-sets")
	class CreateQuestionSet {
		@Test
		@DisplayName("201 Created - 질문 세트 생성 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			QuestionSetRequest request = new QuestionSetRequest(3, topicId1, 1L);

			mockMvc.perform(post("/question-sets")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.level").value(3));
		}
	}

	@Nested
	@DisplayName("PATCH /question-sets/{setId}")
	class UpdateQuestionSet {
		@Test
		@DisplayName("200 OK - 질문 세트 수정 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			QuestionSetRequest request = new QuestionSetRequest(5, topicId2, 2L);

			mockMvc.perform(patch("/question-sets/{setId}", savedSetId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.level").value(5));
		}
	}

	@Nested
	@DisplayName("DELETE /question-sets/{setId}")
	class DeleteQuestionSet {
		@Test
		@DisplayName("204 No Content - 질문 세트 삭제 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			mockMvc.perform(delete("/question-sets/{setId}", savedSetId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNoContent());
		}
	}
}
