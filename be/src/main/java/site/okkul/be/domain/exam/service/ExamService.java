package site.okkul.be.domain.exam.service;

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
import site.okkul.be.domain.exam.exception.ExamErrorCode;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicRepository;
import site.okkul.be.global.exception.BusinessException;
import site.okkul.be.infra.alarm.AlarmService;
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

	private final AlarmService alarmService;


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

			if (questionType.equals(QuestionType.INTRODUCE)) {
				questionSet = questionSetRepository.findIntroQuestion(QuestionType.INTRODUCE.getId());
			} else {
				Collections.shuffle(topics);
				for (Topic topic : topics) {
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
				QuestionSet qs = questionSet.get();

				// 1. DB 저장을 위해 Exam 엔티티에 추가 (기존 7개 뒤에 8번부터 예쁘게 붙음)
				exam.getQuestionSets().add(qs);

				// 2. 응답을 위해 반환용 리스트에 추가 (이번에 만든 것만 담김)
				newlyAddedQuestions.add(qs);
			} else {
				log.error("문제 할당 실패 - 레벨: {}, 타입: {}", level, questionType);
				throw new BusinessException(ExamErrorCode.QUESTION_ALLOCATION_FAILED);
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

		Exam exam = examRepository.findByIdAndUserId(examId, userId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		String url = fileStorageService.upload(examQuestionAnswerRequest.file(), "exam/" + examId + "/answer/");


		ExamAnswer examAnswer = ExamAnswer.builder()
				.id(new ExamAnswer.ExamAnswerId(examId, questionOrder))
				.audioUrl(url)
				.exam(exam)
				.sttScript(examQuestionAnswerRequest.sttText())
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();

		examAnswerRepository.save(examAnswer);
	}

	@Transactional
	public void completeExam(Long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND));
		exam.completeExam();
	}
}
