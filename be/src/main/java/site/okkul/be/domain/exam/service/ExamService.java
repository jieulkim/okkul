package site.okkul.be.domain.exam.service;

import jakarta.persistence.EntityNotFoundException;
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
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicRepository;
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
	 * 1단계 문제 생성
	 * - INTRODUCTION: topic/level 무관 랜덤 1개
	 * - 나머지: level + (설문 topic) + type_code 기준으로 세트 랜덤
	 */
	@Transactional
	public ExamDetailResponse createExam(Long userId, Long surveyId) {
		Survey survey = surveyRepository.findBySurveyIdAndUserId(surveyId, userId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 데이터입니다."));

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
				() -> new IllegalArgumentException("존재하지 않는 시험 세션입니다. ID: " + examId)
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
				() -> new IllegalArgumentException("존재하지 않는 시험 세션입니다. ID: " + examId)
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
	@Async
	@Transactional
	public void allocateQuestion(Long examId) {
		log.info("비동기처리: 문제를 할당합니다");
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new IllegalArgumentException("존재하지 않는 시험 세션입니다. ID: " + examId)
		);

		Survey survey = surveyRepository.findBySurveyIdAndUserId(exam.getSurveyId(), exam.getUserId()).orElseThrow(
				() -> new IllegalArgumentException("존재하지 않는 설문 데이터입니다.")
		);

		// 설문조사에 맞게 불러온 문제 유형들
		List<QuestionType> questionTypes;

		Integer level;

		if (exam.getQuestionSets().isEmpty()) {
			// 첫 시험 문제 생성 시
			questionTypes = ExamLevelDesign.getFirstLayoutByLevel(exam.getInitialDifficulty());
			level = exam.getInitialDifficulty();
		} else {
			// 난이도 조정 된 이후 시험 문제 생성 시
			questionTypes = ExamLevelDesign.getRemainingLayoutByLevel(exam.getAdjustedDifficulty());
			level = exam.getAdjustedDifficulty();
		}
		// 사용 가능한 토픽
		List<Topic> topics = getRandomTopics(exam, survey);


		// 문제 가져오기
		for (QuestionType questionType : questionTypes) {
			Optional<QuestionSet> questionSet = Optional.empty();
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

			if (questionSet.isPresent()) {
				exam.getQuestionSets().add(questionSet.get());
			} else {
				log.error("님들아 큰일남 문제가 없음!!!");
			}
		}
		log.info("비동기처리: 문제 할당이 완료 되었습니다.");
		log.info("비동기처리: :{}", exam.getQuestionSets().size());
		examRepository.save(exam);
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
				() -> new IllegalArgumentException("존재하지 않는 시험 세션입니다. ID: " + examId)
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
				.orElseThrow(() -> new EntityNotFoundException("시험 세션을 찾을 수 없습니다."));
		exam.completeExam();
	}
}
