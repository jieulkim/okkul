package site.okkul.be.domain.exam.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "질문 상세 정보")
public class QuestionResponse {

    @Schema(description = "문항 매핑 ID (답변 제출 시 식별자로 사용)", example = "1201")
    private Long answerId; // Exam_questions 테이블의 PK

    @Schema(description = "질문 순서", example = "1")
    private Integer questionOrder;

    @Schema(description = "질문 텍스트 (화면 표시용)", example = "Let's start the interview. Tell me about yourself.")
    private String questionText;

    @Schema(description = "질문 음성 파일 경로 (S3 등 스토리지 URL)", example = "https://cdn.okkul.site/audio/q1.mp3")
    private String audioUrl;
}
