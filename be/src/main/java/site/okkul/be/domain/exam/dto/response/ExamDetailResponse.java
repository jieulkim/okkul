package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import site.okkul.be.domain.exam.entity.Exam;
import site.okkul.be.domain.question.dto.ExamQuestionResponse;
import site.okkul.be.domain.question.entity.QuestionSet;


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

	/**
	 * [부분 응답용] 기본 정보(DTO) + 새로 생성된 문제 리스트 + 시작 번호
	 * (POST startExam, PATCH adjust-level 에서 사용)
	 *
	 * @param baseInfo      서비스에서 받은 기본 시험 정보 DTO
	 * @param newQuestions  새로 할당된 QuestionSet 리스트
	 * @param startSequence 문제 시작 번호 (처음이면 1, 난이도 조절이면 8)
	 */
	public static ExamDetailResponse of(ExamDetailResponse baseInfo, List<QuestionSet> newQuestions, int startSequence) {
		AtomicInteger idx = new AtomicInteger(startSequence);
		List<ExamQuestionResponse> questionResponses = convertToDto(newQuestions, idx);

		return new ExamDetailResponse(
				baseInfo.id(),
				baseInfo.initialDifficulty(),
				baseInfo.adjustedDifficulty(),
				baseInfo.createdAt(),
				questionResponses
		);
	}

	// 중복 로직 분리 (private helper)
	private static List<ExamQuestionResponse> convertToDto(List<QuestionSet> questionSets, AtomicInteger idx) {
		return questionSets.stream()
				.flatMap(questionSet -> questionSet.getQuestions().stream()
						.map(question -> ExamQuestionResponse.from(question, idx.getAndIncrement())))
				.toList();
	}
}
