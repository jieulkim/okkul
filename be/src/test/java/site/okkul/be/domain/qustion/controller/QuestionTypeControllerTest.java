//package site.okkul.be.domain.qustion.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import site.okkul.be.domain.qustion.dto.QuestionTypeRequest;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//class QuestionTypeControllerTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@Autowired
//	private ObjectMapper objectMapper;
//
//	@Nested
//	@DisplayName("GET /question-types")
//	class GetQuestionTypes {
//		@Test
//		@DisplayName("200 OK - 질문 유형 목록 조회 성공")
//		@WithMockUser
//		void success() throws Exception {
//			mockMvc.perform(get("/question-types")
//							.contentType(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(jsonPath("$.content").isArray())
//					.andExpect(jsonPath("$.content[0].typeCode").exists());
//		}
//	}
//
//	@Nested
//	@DisplayName("GET /question-types/{id}")
//	class GetQuestionType {
//		@Test
//		@DisplayName("200 OK - 질문 유형 단건 조회 성공")
//		@WithMockUser
//		void success() throws Exception {
//			Long id = 1L; // data.sql에 존재
//
//			mockMvc.perform(get("/question-types/{id}", id)
//							.contentType(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(jsonPath("$.id").value(id));
//		}
//	}
//
//	@Nested
//	@DisplayName("POST /question-types")
//	class CreateQuestionType {
//		@Test
//		@DisplayName("200 OK - 질문 유형 생성 성공")
//		@WithMockUser(roles = "ADMIN")
//		void success() throws Exception {
//			QuestionTypeRequest request = new QuestionTypeRequest("NEW_TYPE", "New Description");
//
//			mockMvc.perform(post("/question-types")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(objectMapper.writeValueAsString(request)))
//					.andExpect(status().isOk());
//		}
//	}
//
//	@Nested
//	@DisplayName("PATCH /question-types/{id}")
//	class UpdateQuestionType {
//		@Test
//		@DisplayName("200 OK - 질문 유형 수정 성공")
//		@WithMockUser(roles = "ADMIN")
//		void success() throws Exception {
//			Long id = 1L;
//			QuestionTypeRequest request = new QuestionTypeRequest("UPDATED_TYPE", "Updated Description");
//
//			mockMvc.perform(patch("/question-types/{id}", id)
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(objectMapper.writeValueAsString(request)))
//					.andExpect(status().isOk())
//					.andExpect(jsonPath("$.typeCode").value("UPDATED_TYPE"));
//		}
//	}
//
//	@Nested
//	@DisplayName("DELETE /question-types/{id}")
//	class DeleteQuestionType {
//		@Test
//		@DisplayName("204 No Content - 질문 유형 삭제 성공")
//		@WithMockUser(roles = "ADMIN")
//		void success() throws Exception {
//			Long id = 1L;
//
//			mockMvc.perform(delete("/question-types/{id}", id)
//							.contentType(MediaType.APPLICATION_JSON))
//					.andExpect(status().isNoContent());
//		}
//	}
//}
