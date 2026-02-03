package site.okkul.be.domain.exam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.exam.dto.request.ExamCreateRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.service.ExamService;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.repository.QuestionRepository;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.mapper.SurveyMapper;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicRepository;
import site.okkul.be.infra.storage.FileStorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExamControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ExamService examService;

	@Autowired
	private SurveyMapper surveyMapper;

	@Autowired
	private SurveyJpaRepository surveyRepository;

	@Autowired
	private QuestionSetRepository questionSetRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private TopicRepository topicRepository;

	@MockitoBean // 컨텍스트에 있는 FileStorageService를 Mockito Mock 객체로 교체함
	private FileStorageService fileStorageService;

	@BeforeAll
	public void setUp() {
		List<Topic> topics = topicRepository.findAll();
		for (int level = 1; level < 7; level++) {
			for (QuestionType questionType : QuestionType.values()) {
				for (Topic topic : topics) {
					QuestionSet questionSet = questionSetRepository.save(
							QuestionSet.builder()
									.level(level)
									.topic(topic)
									.questionType(questionType)
									.questions(new ArrayList<>())
									.build()
					);
					for (int question_index = 1; question_index < 4; question_index++) {
						questionSet.getQuestions().add(
								Question.builder()
										.questionText(UUID.randomUUID().toString())
										.audioUrl(UUID.randomUUID().toString())
										.order(question_index)
										.questionSet(questionSet)
										.build()
						);
					}
					questionSetRepository.save(questionSet);
				}
			}
		}
	}

	SurveyCreateRequest surveyCreateRequest = SurveyCreateRequest.builder()
			.occupationAnswerId(1)
			.hasJob(true)
			.workPeriodAnswerId(1)
			.teachAnswerId(1)
			.manager(true)
			.student(true)
			.classTypeAnswerId(1)
			.residenceAnswerId(1)
			.leisure(new ArrayList<>(Arrays.asList(101L, 102L)))
			.hobby(new ArrayList<>(Arrays.asList(301L, 302L)))
			.exercise(new ArrayList<>(Arrays.asList(401L, 402L, 403L, 404L, 405L)))
			.level(5)
			.build();

	@Nested
	@DisplayName("POST /exam")
	class StartExam {
		@Test
		@DisplayName("201 OK - 시험 시작 성공")
		@WithMockUser(username = "1")
		void success() throws Exception {

			Survey survey = surveyRepository.save(surveyMapper.toEntity(1L, surveyCreateRequest));

			mockMvc.perform(post("/exam")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(new ExamCreateRequest(survey.getSurveyId()))))
					.andExpect(status().isCreated());
		}
	}

	@Nested
	@DisplayName("PATCH /exam/{examId}/adjust-level")
	class AdjustLevel {
		@Test
		@DisplayName("200 OK - 난이도 조정 및 나머지 문항 조회 성공")
		@WithMockUser(username = "1")
		void success() throws Exception {
			int adjustedDifficulty = 6;

			Survey survey = surveyRepository.save(surveyMapper.toEntity(1L, surveyCreateRequest));
			ExamDetailResponse exam = examService.createExam(1L, survey.getSurveyId());

			mockMvc.perform(patch("/exam/{examId}/adjust-level", exam.id())
							.with(csrf())
							.param("adjustedDifficulty", String.valueOf(adjustedDifficulty))
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());

		}

		@Test
		@DisplayName("400 Bad Request - 잘못된 난이도 값")
		@WithMockUser(username = "1")
		void fail_invalidDifficulty() throws Exception {
			Survey survey = surveyRepository.save(surveyMapper.toEntity(1L, surveyCreateRequest));
			ExamDetailResponse exam = examService.createExam(1L, survey.getSurveyId());

			int adjustedDifficulty = survey.getLevel() + 2;

			mockMvc.perform(patch("/exam/{examId}/adjust-level", exam.id())
							.with(csrf())
							.param("adjustedDifficulty", String.valueOf(adjustedDifficulty))
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest()); // 이제 500이 아닌 400을 기대함
		}
	}

	@Nested
	@DisplayName("POST /exam/{examId}/answers/{answerId}")
	class SubmitAnswer {
		@Test
		@DisplayName("202 Accepted - 답변 제출 성공")
		@WithMockUser(username = "1")
		void success() throws Exception {
			given(fileStorageService.upload(any(), any())).willReturn("http://fake-url.com");

			Survey survey = surveyRepository.save(surveyMapper.toEntity(1L, surveyCreateRequest));
			ExamDetailResponse exam = examService.createExam(1L, survey.getSurveyId());
			examService.allocateQuestion(exam.id());

			Long answerId = 1L;
			MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "audio content".getBytes());

			mockMvc.perform(multipart("/exam/{examId}/answers/{answerId}", exam.id(), answerId)
							.file(file)
							.with(csrf())
							.contentType(MediaType.MULTIPART_FORM_DATA))
					.andExpect(status().isAccepted());

		}
	}

	@Nested
	@DisplayName("POST /exam/{examId}/complete")
	class CompleteExam {
		@Test
		@DisplayName("200 OK - 시험 종료 성공")
		@WithMockUser(username = "1")
		void success() throws Exception {
			Survey survey = surveyRepository.save(surveyMapper.toEntity(1L, surveyCreateRequest));
			ExamDetailResponse exam = examService.createExam(1L, survey.getSurveyId());


			mockMvc.perform(post("/exam/{examId}/complete", exam.id())
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		}
	}
}
