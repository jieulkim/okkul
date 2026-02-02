package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import site.okkul.be.domain.exam.entity.Exam;
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
		List<ExamQuestionResponse> questions
) {
	public static ExamDetailResponse from(Exam exam) {
		// 스트림용 인덱스
		AtomicInteger idx = new AtomicInteger(1);
		List<ExamQuestionResponse> questionResponses = exam.getQuestionSets().stream()
				.flatMap(questionSet -> questionSet.getQuestions().stream()
						.map(question -> ExamQuestionResponse.from(question, idx.getAndIncrement())))
				.toList();

		return new ExamDetailResponse(
				exam.getId(),
				exam.getInitialDifficulty(),
				exam.getAdjustedDifficulty(),
				exam.getCreatedAt(),
				questionResponses
		);
	}
}
