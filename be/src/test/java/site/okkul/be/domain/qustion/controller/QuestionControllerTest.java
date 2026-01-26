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
import site.okkul.be.domain.qustion.dto.QuestionRequest;

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

	@Nested
	@DisplayName("POST /question-sets/{setId}/questions")
	class AddQuestion {
		@Test
		@DisplayName("201 Created - 질문 추가 성공")
		@WithMockUser(roles = "ADMIN")
		void success() throws Exception {
			Long setId = 1L; // data.sql에 존재
			QuestionRequest request = new QuestionRequest("New Question", "http://example.com/new.mp3", 2);

			mockMvc.perform(post("/question-sets/{setId}/questions", setId)
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
			Long setId = 1L;
			Long questionId = 1L; // data.sql에 존재
			QuestionRequest request = new QuestionRequest("Updated Question", "http://example.com/updated.mp3", 1);

			mockMvc.perform(patch("/question-sets/{setId}/questions/{questionId}", setId, questionId)
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
			Long setId = 1L;
			Long questionId = 1L; // data.sql에 존재

			mockMvc.perform(delete("/question-sets/{setId}/questions/{questionId}", setId, questionId)
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNoContent());
		}
	}
}
