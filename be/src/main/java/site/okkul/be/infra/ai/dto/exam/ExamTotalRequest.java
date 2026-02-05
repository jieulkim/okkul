package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import site.okkul.be.domain.exam.entity.Exam;

public record ExamTotalRequest(
		@JsonProperty("exam_id")
		Long examId,

		@JsonProperty("answers")
		List<AnswerSummaryDto> answers // 전체 문항의 답변과 점수 요약본
) {

	public static ExamTotalRequest from(Exam exam) {
		return new ExamTotalRequest(
				exam.getId(),
				exam.getExamAnswers().stream().map(AnswerSummaryDto::from).toList()
		);
	}

}
