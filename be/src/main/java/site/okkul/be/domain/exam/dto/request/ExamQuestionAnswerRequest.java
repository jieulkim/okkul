package site.okkul.be.domain.exam.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "시험 문항 답변 저장 요청 객체")
public record ExamQuestionAnswerRequest(
		@Schema(description = "음성 파일")
		@Parameter(required = true)
		MultipartFile file,
		@Schema(description = "STT 텍스트")
		@Parameter(required = true)
		String sttText,
		@Schema(description = "답변 길이(초)", example = "45")
		@Parameter(required = true)
		Integer duration
) {
}
