package site.okkul.be.domain.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.service.PracticeService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PracticeService practiceService;

    @Nested
    @DisplayName("POST /practices/{practiceId}/feedback")
    class SavePracticeSession {

        @Test
        @DisplayName("202 Accepted - AI 피드백 요청 성공")
        @WithMockUser(username = "1", roles = "USER")
        void success() throws Exception {
            // given
            long practiceId = 1L;
            long practiceAnswerId = 123L;
            long questionId = 10L;
            var requestDto = new PracticeFeedbackRequest(questionId, "korean script", "english script");
            var audioFile = new MockMultipartFile("audio", "test.mp3", "audio/mpeg", "audio data".getBytes());

            // DTO를 JSON 형태의 멀티파트 파일로 변환
            var requestJson = objectMapper.writeValueAsString(requestDto);
            var requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes(StandardCharsets.UTF_8));

            given(practiceService.createAnswerAndRequestFeedbackAsync(
                    eq(practiceId),
                    any(PracticeFeedbackRequest.class),
                    any(MockMultipartFile.class),
                    eq(1L)))
                    .willReturn(practiceAnswerId);

            // when & then
            mockMvc.perform(multipart("/practices/{practiceId}/feedback", practiceId)
                            .file(audioFile)
                            .file(requestPart)
                            .param("ai", "true")
                            .with(csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.practiceAnswerId").value(practiceAnswerId));

            then(practiceService).should().createAnswerAndRequestFeedbackAsync(
                    eq(practiceId),
                    any(PracticeFeedbackRequest.class),
                    any(MockMultipartFile.class),
                    eq(1L));
        }
    }
}
