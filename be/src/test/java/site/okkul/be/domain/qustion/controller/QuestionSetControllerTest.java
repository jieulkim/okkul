package site.okkul.be.domain.qustion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content[0].setId").exists());
		}
	}

	@Nested
	@DisplayName("GET /question-sets/{setId}")
	class GetQuestionSet {
		@Test
		@DisplayName("200 OK - 질문 세트 단건 조회 성공")
		@WithMockUser
		void success() throws Exception {
			// data.sql에 set_id=1 데이터가 있음
			Long setId = 1L;

			mockMvc.perform(get("/question-sets/{setId}", setId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.setId").value(setId));
		}
	}

	@Nested
	@DisplayName("POST /question-sets")
	class CreateQuestionSet {
		@Test
		@DisplayName("201 Created - 질문 세트 생성 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			QuestionSetRequest request = new QuestionSetRequest(3, 101L, 1L);

			mockMvc.perform(post("/question-sets")
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
			Long setId = 1L;
			QuestionSetRequest request = new QuestionSetRequest(5, 102L, 2L);

			mockMvc.perform(patch("/question-sets/{setId}", setId)
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
			Long setId = 1L;

			mockMvc.perform(delete("/question-sets/{setId}", setId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNoContent());
		}
	}
}
