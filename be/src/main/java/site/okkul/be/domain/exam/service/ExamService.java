package site.okkul.be.domain.exam.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.exam.dto.response.ExamDetailResponse;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamReport;
import site.okkul.be.domain.exam.entity.ExamStatus;
import site.okkul.be.domain.exam.exception.ExamErrorCode;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.exam.repository.ExamReportJpaRepository;
import site.okkul.be.domain.survey.entity.Survey;
import site.okkul.be.domain.survey.repository.SurveyJpaRepository;
import site.okkul.be.global.exception.BusinessException;
import site.okkul.be.global.exception.SystemException;
import site.okkul.be.infra.ai.AiClientProvider;
import site.okkul.be.infra.ai.dto.exam.AnswerSummaryDto;
import site.okkul.be.infra.ai.dto.exam.ExamTotalAnalysisResponse;

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
	 * Survey 레포지토리
	 */
	private final SurveyJpaRepository surveyRepository;

	/**
	 * AI 서버용 기능
	 */
	private final AiClientProvider aiClientProvider;

	private final ExamReportJpaRepository examReportJpaRepository;

	private ExamService self;

	@Autowired
	public void setSelf(@Lazy ExamService self) {
		this.self = self;
	}


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


	@Transactional
	public void completeExam(Long examId) {
		// 1. 시험 검색
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);
		// 2. AI 피드백 중복 생성 방지
		if (exam.getStatus() == ExamStatus.ANALYZING || exam.getStatus() == ExamStatus.COMPLETED) {
			throw new BusinessException(ExamErrorCode.EXAM_ALREADY_ENDED);
		}
		// 3. 시험 분석 단계로 넘어감
		self.updateExamStatus(examId, ExamStatus.ANALYZING);
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
		self.updateExamStatus(examId, ExamStatus.ANALYZING);
		for (int i = 0; i < 3; i++) {
			ResponseEntity<ExamTotalAnalysisResponse> response = aiClientProvider
					.getClient(useRealAi)
					.analyzeTotalExam(
							exam.getExamAnswers().stream().map(AnswerSummaryDto::from).toList()
					);

			if (response.getStatusCode().is2xxSuccessful()) {
				// 4. 리포트 저장하기
				self.updateExamStatus(examId, ExamStatus.COMPLETED);
				examReportJpaRepository.save(ExamReport.createReport(
						exam,
						BigDecimal.valueOf(response.getBody().averageGrammarScore()),
						BigDecimal.valueOf(response.getBody().averageVocabScore()),
						BigDecimal.valueOf(response.getBody().averageLogicScore()),
						BigDecimal.valueOf(response.getBody().averageFluencyScore()),
						BigDecimal.valueOf(response.getBody().averageRelevanceScore()),
						BigDecimal.valueOf(response.getBody().totalScore()),
						response.getBody().predictedLevel(),
						response.getBody().strengths().toString(),
						response.getBody().improvements().toString(),
						""
				));
				return;
			}
		}
		throw new SystemException(ExamErrorCode.AI_SERVER_ERROR,
				"Exam Report 생성 실패",
				"AI 서버 응답을 3회 모두 받지 못했습니다."
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateExamStatus(Long examId, ExamStatus status) {
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);
		exam.updateStatus(status);
	}
}
