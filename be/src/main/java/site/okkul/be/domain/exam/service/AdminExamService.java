package site.okkul.be.domain.exam.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.exam.entity.AnswerStatus;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.entity.ExamReport;
import site.okkul.be.domain.exam.entity.ExamSentenceFeedback;
import site.okkul.be.domain.exam.entity.ExamStatus;
import site.okkul.be.domain.exam.exception.ExamErrorCode;
import site.okkul.be.domain.exam.repository.ExamAnswerJpaRepository;
import site.okkul.be.domain.exam.repository.ExamJpaRepository;
import site.okkul.be.domain.exam.repository.ExamReportJpaRepository;
import site.okkul.be.global.exception.BusinessException;
import site.okkul.be.global.exception.SystemException;
import site.okkul.be.infra.ai.AiClientProvider;
import site.okkul.be.infra.ai.dto.exam.AnswerSummaryDto;
import site.okkul.be.infra.ai.dto.exam.ExamTotalAnalysisResponse;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisRequest;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminExamService {

	private final ExamJpaRepository examRepository;
	private final ExamReportJpaRepository examReportJpaRepository;
	private final AiClientProvider aiClientProvider;
	private final ExamAnswerJpaRepository examAnswerRepository;

	private final ExamService examService;
	private final ExamAnswerService examAnswerService;


	@Transactional
	@Async
	public void adminExamReportRecreate(Long examId) {
		// 1. 시험 검색
		Exam exam = examRepository.findById(examId).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);

		Optional<ExamReport> originReport = examReportJpaRepository.findById(examId);
		originReport.ifPresent(examReportJpaRepository::delete);

		// 3. Ai 클라이언트를 가져와서 분석 진행하기
		examService.updateExamStatus(examId, ExamStatus.ANALYZING);
		for (int i = 0; i < 3; i++) {
			ResponseEntity<ExamTotalAnalysisResponse> response = aiClientProvider
					.getClient(true)
					.analyzeTotalExam(
							exam.getExamAnswers().stream().map(AnswerSummaryDto::from).toList()
					);

			if (response.getStatusCode().is2xxSuccessful()) {
				// 4. 리포트 저장하기
				log.info("{}", response.getBody());
				examService.updateExamStatus(examId, ExamStatus.COMPLETED);
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
		examService.updateExamStatus(examId, ExamStatus.ANALYZING_FAILED);
		throw new SystemException(ExamErrorCode.AI_SERVER_ERROR,
				"Exam Report 생성 실패",
				"AI 서버 응답을 3회 모두 받지 못했습니다."
		);
	}


	@Async
	@Transactional
	public void adminExamAnswerReportRecreate(Long examId, Integer questionOrder) {
		// 1. 시험 답변 가져오기
		ExamAnswer examAnswer = examAnswerRepository.findById(
				new ExamAnswer.ExamAnswerId(examId, questionOrder)
		).orElseThrow(
				() -> new BusinessException(ExamErrorCode.EXAM_NOT_FOUND)
		);


		// 2. AI서버에서 답변 분석 진행하기
		examAnswerService.updateExamAnswerStatus(
				new ExamAnswer.ExamAnswerId(examId, questionOrder),
				AnswerStatus.ANALYZING
		);

		// 최대 3회 시도 하기
		for (int i = 0; i < 3; i++) {
			ResponseEntity<QuestionAnalysisResponse> questionAnalysisResponseTemp = aiClientProvider
					.getClient(true)
					.analyzeQuestion(
							QuestionAnalysisRequest.from(
									examAnswer.getExam().getQuestions().get(questionOrder - 1),
									examAnswer
							)
					);
			if (questionAnalysisResponseTemp.getStatusCode().is2xxSuccessful()) {
				// 3. 분석결과 DB에 적용하기
				log.info("{}", questionAnalysisResponseTemp.getBody());
				QuestionAnalysisResponse questionAnalysisResponse = questionAnalysisResponseTemp.getBody();
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
				examAnswerService.updateExamAnswerStatus(
						new ExamAnswer.ExamAnswerId(examId, questionOrder),
						AnswerStatus.COMPLETED
				);
				return;
			} else {
				// Answer 분석 실패
				log.error("Exam Answer 분석 생성 실패 - {}회 실패 재시도 합니다... ", i);
				examAnswerService.updateExamAnswerStatus(
						new ExamAnswer.ExamAnswerId(examId, questionOrder),
						AnswerStatus.ANALYZING_FAILED
				);

			}
		}
		throw new SystemException(
				ExamErrorCode.AI_SERVER_ERROR,
				"문제 리포트 생성이 실패했습니다: AI 서버 응답을 3회 모두 받지 못했습니다.",
				String.format("ExamId=%d, QuestionOrder=%d, Attempts=%d, Reason=%s",
						examId, questionOrder, 3, "AI서버 비정상 응답/타임아웃")
		);
	}
}
