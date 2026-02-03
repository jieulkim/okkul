package site.okkul.be.infra.ai;

import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;
import site.okkul.be.infra.ai.dto.exam.AnswerSummaryDto;
import site.okkul.be.infra.ai.dto.exam.ExamTotalAnalysisResponse;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisRequest;
import site.okkul.be.infra.ai.dto.exam.QuestionAnalysisResponse;

/**
 * AI 서버와 통신하기 위한 선언적 HTTP 클라이언트 인터페이스입니다.
 */
public interface AiClient {

	/**
	 * 유형별 연습용 답변 분석 기능
	 *
	 * @param request 요청 객체
	 * @return AI 피드백 요청 결과
	 */
	@PostExchange("/v1/analyze")
	AiFeedbackResponse requestFeedback(@RequestBody AiFeedbackRequest request);

	/**
	 * 1. 개별 문제 분석 API
	 * 한 문제에 대한 상세 피드백과 문장별 교정을 요청합니다.
	 */
	@PostExchange("/v1/analyze/exam")
	QuestionAnalysisResponse analyzeQuestion(@RequestBody QuestionAnalysisRequest request);

	/**
	 * 2. 모의고사 총체적 분석 API
	 * 전체 답변들의 흐름을 파악하여 종합 등급과 총평을 요청합니다.
	 */
	@PostExchange("/v1/analyze/exam-feedback")
	ExamTotalAnalysisResponse analyzeTotalExam(@RequestBody List<AnswerSummaryDto> request);
}
