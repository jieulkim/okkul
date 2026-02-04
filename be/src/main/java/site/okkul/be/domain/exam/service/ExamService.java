package site.okkul.be.domain.exam.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.exam.dto.request.ExamQuestionAnswerRequest;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.entity.ExamReport;
import site.okkul.be.domain.exam.entity.ExamSentenceFeedback;
import site.okkul.be.domain.exam.exception.ExamErrorCode;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.exam.repository.ExamReportJpaRepository;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicRepository;
import site.okkul.be.global.exception.BusinessException;
import site.okkul.be.global.exception.SystemException;
import site.okkul.be.infra.ai.AiClientProvider;
import site.okkul.be.infra.ai.dto.exam.AnswerSummaryDto;
import site.okkul.be.infra.ai.dto.exam.ExamTotalAnalysisResponse;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisRequest;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisResponse;
import site.okkul.be.infra.storage.FileStorageService;

/**
 * 모의고사 비지니스 로직 서비스
 * <p>
 * 구조(표준 오픽에 가깝게):
 * - startExam(): 1~7번까지만 출제/저장
 * - getRemainingQuestions(): 8번~마지막 출제/저장 (7번 이후 난이도 조정 반영)
 * <p>
 * Topic 다양성:
 * - 시험 시작 시 설문 Topic을 셔플해서 Exam.topicOrder에 저장
 * - 출제할 때마다 Exam.topicCursor로 다음 topic을 가져오고 +1
 * - 난이도 조정 후에도 같은 topic 흐름을 이어감
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

	/**
	 * Exam 레포지토리
	 */
	private final ExamJpaRepository examRepository;
	/**
	 * Exam Answer 레포지토리
	 */
	private final ExamAnswerJpaRepository examAnswerRepository;
	/**
	 * Survey 레포지토리
	 */
	private final SurveyJpaRepository surveyRepository;
	/**
	 * Question Set 레포지토리
	 */
	private final QuestionSetRepository questionSetRepository;

	/**
	 * Topic 레포지토리
	 */
	private final TopicRepository topicRepository;

	/**
	 * 파일 스토리지 서비스
	 */
	private final FileStorageService fileStorageService;

	/**
	 * AI 서버용 기능
	 */
	private final AiClientProvider aiClientProvider;

	private final ExamReportJpaRepository examReportJpaRepository;


	/**
	 * 1단계 문제 생성
	 * - INTRODUCTION: topic/level 무관 랜덤 1개
	 * - 나머지: level + (설문 topic) + type_code 기준으로 세트 랜덤
	 */
	@Transactional
	public ExamDetailResponse createExam(Long userId, Long surveyId) {
		Survey survey = surveyRepository.findBySurveyIdAndUserId(surveyId, userId)
				.orElseThrow(() -> new BusinessException(ExamErrorCode.SURVEY_NOT_FOUND));

		Exam exam = examRepository.save(
				Exam.create(
						survey.getSurveyId(),
						survey.getLevel(),
						userId
				)
		);

		return ExamDetailResponse.from(exam);
	}

	@Transactional(readOnly = true)
	public ExamDetailResponse getExamInfoDetails(Long userId, Long examId) {
		Exam exam = examRepository.findByIdAndUserId(examId, userId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		return ExamDetailResponse.from(exam);
	}

	/**
	 * 문제 레벨 업데이트
	 *
	 * @param userId   유저 ID
	 * @param examId   시험 ID
	 * @param newLevel 뉴 레벨
	 * @return 업데이트된 시험 세션
	 */
	@Transactional
	public ExamDetailResponse updateLevel(Long userId, Long examId, Integer newLevel) {
		Exam exam = examRepository.findByIdAndUserId(examId, userId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);
		exam.updateAdjustedDifficulty(newLevel);
		return ExamDetailResponse.from(exam);
	}

	/**
	 * 문제 할당하는 함수
	 * <p>
	 * 1. 설문조사에서 문제 토픽 리스트를 가져옴
	 * 2. 1차에서 사용된 토픽들은 제외하고 랜덤으로 토픽을 가져옵니다
	 * 3. 문제셋을 랜덤으로 가져옵니
	 * </p>
	 *
	 * @param examId 시험 ID
	 */
	@Transactional
	public List<QuestionSet> allocateQuestion(Long examId) {
		log.info("문제 할당 프로세스 시작 - ExamId: {}", examId);

		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		Survey survey = surveyRepository.findBySurveyIdAndUserId(exam.getSurveyId(), exam.getUserId()).orElseThrow(
				() -> new BusinessException(ExamErrorCode.SURVEY_NOT_FOUND)
		);

		List<QuestionType> questionTypes;
		Integer level;

		// 이번 요청으로 새로 생성된 문제들만 담을 리스트 (Controller 반환용)
		List<QuestionSet> newlyAddedQuestions = new ArrayList<>();

		if (exam.getQuestionSets().isEmpty()) {
			log.info("최초 문제 할당: 1번~7번 레이아웃 적용 (초기 난이도: {})", exam.getInitialDifficulty());
			questionTypes = ExamLevelDesign.getFirstLayoutByLevel(exam.getInitialDifficulty());
			level = exam.getInitialDifficulty();
		} else {
			log.info("추가 문제 할당: 8번 이후 레이아웃 적용 (조정 난이도: {})", exam.getAdjustedDifficulty());
			questionTypes = ExamLevelDesign.getRemainingLayoutByLevel(exam.getAdjustedDifficulty());
			level = exam.getAdjustedDifficulty();
		}

		// 사용 가능한 토픽 가져오기
		List<Topic> topics = getRandomTopics(exam, survey);

		// 문제 가져오기 및 할당
		for (QuestionType questionType : questionTypes) {
			Optional<QuestionSet> questionSet = Optional.empty();
			List<Topic> triedTopics = new ArrayList<>(); // 시도한 토픽 기록용
			Topic lastTopic = null;

			if (questionType.equals(QuestionType.INTRODUCE)) {
				questionSet = questionSetRepository.findIntroQuestion(QuestionType.INTRODUCE.getId());
			} else {
				Collections.shuffle(topics);
				for (Topic topic : topics) {
					lastTopic = topic;
					triedTopics.add(topic);
					questionSet = questionSetRepository.findRandomByLevelAndTopic(
							level,
							topic.getId(),
							questionType
					);
					if (questionSet.isPresent()) {
						break;
					}
				}
			}

			if (questionSet.isPresent()) {
				// 1. DB 저장을 위해 Exam 엔티티에 추가 (기존 7개 뒤에 8번부터 예쁘게 붙음)
				QuestionSet qs = questionSet.get();
				exam.getQuestionSets().add(qs);
				exam.getQuestions().addAll(qs.getQuestions());

				// 2. 응답을 위해 반환용 리스트에 추가 (이번에 만든 것만 담김)
				newlyAddedQuestions.add(qs);
			} else {
				log.error("문제 할당 실패 - 레벨: {}, 타입: {}", level, questionType);
				String errorMessage = createErrorMessage(exam.getId(), level, questionType, topics, triedTopics, lastTopic, survey);
				throw new SystemException(ExamErrorCode.QUESTION_ALLOCATION_FAILED, "문제 할당 실패", errorMessage);
			}
		}

		log.info("문제 할당 완료. 신규 추가 문항 수: {}", newlyAddedQuestions.size());
		examRepository.save(exam);

		return newlyAddedQuestions;
	}

	/**
	 * 설문조사에서 토픽을 랜덤으로 가져와야함
	 * 만약 2번째 주제를 가져오는 상황이라면, 기존에 선택된 토픽들은 제거하고 가져와야 함
	 */
	private List<Topic> getRandomTopics(Exam exam, Survey survey) {
		// 사용가능한 토픽들
		List<Topic> availableTopics = survey.getTopicIds().stream().map(topicRepository::findById)
				.filter(Optional::isPresent)
				.map(Optional::get).toList();

		// 이미 사용한 토픽들
		List<Topic> usedTopic = new ArrayList<>();
		for (QuestionSet qs : exam.getQuestionSets()) {
			usedTopic.add(qs.getTopic());
		}

		// 생성할 토픽들 = 사용가능한 토픽들 - 이미 사용한 토픽들
		List<Topic> reuslt = new ArrayList<>();
		for (Topic t : availableTopics) {
			if (!usedTopic.contains(t)) {
				reuslt.add(t);
			}
		}

		// 랜덤 셔플
		Collections.shuffle(reuslt);

		return reuslt;
	}

	/**
	 * 답변 제출
	 * - answerId(=ExamAnswer PK)로 문항을 식별
	 */
	@Transactional
	public void submitAnswer(Long examId, Integer questionOrder, ExamQuestionAnswerRequest examQuestionAnswerRequest, Long userId) {
		// 1. 시험 존재 여부 체크
		Exam exam = examRepository.findByIdAndUserId(examId, userId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		// 2. 시험 종료 여부 확인
		if (exam.getEndAt() != null) {
			throw new BusinessException(ExamErrorCode.EXAM_ALREADY_ENDED);
		}
		// 3. 문제 존재 여부 확인
		if (exam.getQuestions().size() < questionOrder) {
			throw new BusinessException(ExamErrorCode.QUESTION_NOT_FOUND);
		}

		// 4. 답변 중복 체크
		ExamAnswer.ExamAnswerId examAnswerId = new ExamAnswer.ExamAnswerId(examId, questionOrder);
		if (examAnswerRepository.existsById(examAnswerId)) {
			throw new BusinessException(ExamErrorCode.EXAM_ANSWER_ALREADY_SUBMITTED);
		}

		// 5. 음성 파일 저장
		String url = fileStorageService.upload(examQuestionAnswerRequest.file(), "exam/" + examId + "/answer");

		// 6. 답변 저장
		examAnswerRepository.save(ExamAnswer.builder()
				.id(examAnswerId)
				.audioUrl(url)
				.exam(exam)
				.userAnswer(examQuestionAnswerRequest.sttText())
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build());
	}

	@Async
	@Transactional
	public void feedbackAnswer(Long examId, Integer questionOrder, boolean useRealAi) {
		// 1. 시험 답변 가져오기
		ExamAnswer examAnswer = examAnswerRepository.findById(
				new ExamAnswer.ExamAnswerId(examId, questionOrder)
		).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		// 2. AI서버에서 답변 분석 진행하기
		QuestionAnalysisResponse questionAnalysisResponse = aiClientProvider.getClient(useRealAi).analyzeQuestion(
				QuestionAnalysisRequest.from(
						examAnswer.getExam().getQuestions().get(questionOrder - 1),
						examAnswer
				)
		);

		// 3. 분석결과 DB에 적용하기
		examAnswer.updateFromAi(
				questionAnalysisResponse.grammarScore(),
				questionAnalysisResponse.vocabScore(),
				questionAnalysisResponse.logicScore(),
				questionAnalysisResponse.fluencyScore(),
				questionAnalysisResponse.relevanceScore(),
				questionAnalysisResponse.improvedAnswer(),
				questionAnalysisResponse.logicFeedback(),
				questionAnalysisResponse.fluencyFeedback(),
				questionAnalysisResponse.relevanceFeedback(),
				questionAnalysisResponse.sentenceFeedbacks() == null || questionAnalysisResponse.sentenceFeedbacks().isEmpty()
						? new ArrayList<>()
						: questionAnalysisResponse.sentenceFeedbacks().stream().map(
						dto -> new ExamSentenceFeedback(
								dto.targetSentence(),
								dto.targetSegment(),
								dto.correctedSegment(),
								dto.comment(),
								dto.sentenceOrder(),
								Instant.now()
						)).toList()
		);
	}

	@Transactional
	public void completeExam(Long examId) {
		// 1. 시험 검색
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);
		// 2. 이미 완료된 시험이라면 예외 발생 (이후에 AI 분석을 막기 위함)
		if (exam.getEndAt() != null) {
			throw new BusinessException(ExamErrorCode.EXAM_ALREADY_ENDED);
		}
		// 3. 시험 완료 처리
		exam.completeExam();
	}


	@Transactional
	@Async
	public void examCreateReport(Long examId, boolean useRealAi) {
		// 1. 시험 검색
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		// 2. 이미 리포트가 생성되어 있다면 예외 발생
		if (examReportJpaRepository.existsById(examId)) {
			throw new BusinessException(ExamErrorCode.EXAM_REPORT_ALREADY_CREATED);
		}

		// 3. Ai 클라이언트를 가져와서 분석 진행하기
		ExamTotalAnalysisResponse response = aiClientProvider
				.getClient(useRealAi)
				.analyzeTotalExam(
						exam.getExamAnswers().stream().map(AnswerSummaryDto::from).toList()
				);

		// 4. 리포트 저장하기
		examReportJpaRepository.save(ExamReport.createReport(
				exam,
				BigDecimal.valueOf(response.averageGrammarScore()),
				BigDecimal.valueOf(response.averageVocabScore()),
				BigDecimal.valueOf(response.averageLogicScore()),
				BigDecimal.valueOf(response.averageFluencyScore()),
				BigDecimal.valueOf(response.averageRelevanceScore()),
				BigDecimal.valueOf(response.totalScore()),
				response.predictedLevel(),
				response.strengths().toString(),
				response.improvements().toString(),
				""
		));
	}

	/**
	 * MatterMost, discord등 마크다운 기반 웹훅 전송 시 사용되는 메시지 템플릿
	 *
	 * @param examId          시험 ID
	 * @param level           시험 레벨
	 * @param questionType    문항 유형
	 * @param availableTopics 사용 가능한 토픽
	 * @param triedTopics     시도한 토픽
	 * @param lastTopic       마지막으로 시도한 토픽
	 * @param survey          설문조사 엔티티
	 * @return 메시지
	 */
	private String createErrorMessage(Long examId, Integer level, QuestionType questionType, List<Topic> availableTopics, List<Topic> triedTopics, Topic lastTopic, Survey survey) {
		String lastTopicStr = (lastTopic == null) ? "None (Intro or Logic Error)" : String.format("`%s` (ID: %d)", lastTopic.getTopicName(), lastTopic.getId());

		String availableTopicsStr = (availableTopics == null || availableTopics.isEmpty())
				? "None (Empty List)"
				: availableTopics.stream().map(Topic::getTopicName).toList().toString();

		String triedTopicsStr = (triedTopics == null || triedTopics.isEmpty())
				? "None (Intro or Logic Error)"
				: triedTopics.stream().map(t -> String.format("%s(ID:%d)", t.getTopicName(), t.getId())).toList().toString();

		String surveyTopicIdsStr = (survey == null) ? "Unknown" : survey.getTopicIds().toString();
		String lastTopicName = (lastTopic != null) ? lastTopic.getTopicName() : "None";

		return """
				### 🚨 문제 할당 실패 상세 리포트
				
				| 항목 | 내용 |
				| --- | --- |
				| **Exam ID** | `%d` |
				| **Target Level** | `%d` |
				| **Target Question Type** | `%s` (%s) |
				| **Last Tried Topic** | %s |
				| **Available Topics (Pool)** | %s |
				| **Actually Tried Topics** | %s |
				| **Survey Selected Topic IDs** | `%s` |
				
				**Reason**
				> 위 조건(Level + Type + Tried Topics)에 매칭되는 QuestionSet을 DB에서 찾을 수 없습니다.
				> 특히 마지막으로 시도한 토픽 **%s**에 해당하는 문제가 부족할 가능성이 높습니다.
				
				**Action**
				> DB에 해당 조건의 문제 세트가 존재하는지 확인해주세요.
				""".formatted(
				examId,
				level,
				questionType.getTypeCode(), questionType.getDescription(),
				lastTopicStr,
				availableTopicsStr,
				triedTopicsStr,
				surveyTopicIdsStr,
				lastTopicName
		);


	}
}
