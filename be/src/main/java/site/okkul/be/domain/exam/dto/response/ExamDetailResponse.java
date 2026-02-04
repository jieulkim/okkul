package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import site.okkul.be.domain.exam.entity.AnswerStatus;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.exam.entity.ExamAnswer;
import site.okkul.be.domain.exam.entity.ExamStatus;
import site.okkul.be.domain.question.dto.ExamQuestionResponse;


@Schema(description = "모의고사용 문제 정보")
public record ExamDetailResponse(
		@Schema(description = "시험 ID", example = "1L")
		Long id,
		@Schema(description = "처음 선택한 난이도", example = "5")
		Integer initialDifficulty,
		@Schema(description = "변경한 난이도", example = "6")
		Integer adjustedDifficulty,
		@Schema(description = "생성시간")
		Instant createdAt,
		@Schema(description = "현재 할당된 문제들")
		List<ExamQuestionResponse> questions,
		@Schema(description = "시험 상태")
		ExamStatus examStatus
) {
	public static ExamDetailResponse from(Exam exam) {
		// ExamAnswer를 questionOrder 기준으로 Map으로 변환하여 조회 효율성 및 안전성 확보
		Map<Integer, ExamAnswer> answerMap = exam.getExamAnswers().stream()
				.collect(Collectors.toMap(
						a -> a.getId().getQuestionOrder(),
						a -> a,
						(existing, replacement) -> existing
				));

		List<ExamQuestionResponse> questionResponses = new ArrayList<>();
		// questions 리스트의 인덱스 + 1을 questionOrder로 사용 (@OrderColumn)
		for (int i = 0; i < exam.getQuestions().size(); i++) {
			int order = i + 1;
			ExamAnswer answer = answerMap.get(order);
			AnswerStatus status = (answer != null) ? answer.getStatus() : AnswerStatus.READY;

			questionResponses.add(
					ExamQuestionResponse.from(
							exam.getQuestions().get(i),
							status,
							order
					)
			);
		}

		return new ExamDetailResponse(
				exam.getId(),
				exam.getInitialDifficulty(),
				exam.getAdjustedDifficulty(),
				exam.getCreatedAt(),
				questionResponses,
				exam.getStatus()
		);
	}

	/**
	 * 문제들 서브리스트 하는 함수
	 *
	 * @param start 시작 번호 1번부터~
	 * @param end   마지막 번호 2번부터~
	 * @return 서브리스트
	 */
	public ExamDetailResponse questionSubList(int start, int end) {
		return new ExamDetailResponse(
				this.id,
				this.initialDifficulty,
				this.adjustedDifficulty,
				this.createdAt,
				this.questions.subList(start - 1, end - 1),
				this.examStatus
		);
	}
}
