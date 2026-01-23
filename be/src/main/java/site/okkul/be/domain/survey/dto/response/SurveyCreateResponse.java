package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "설문조사 생성 응답 DTO")
public class SurveyCreateResponse {
    @Schema(description = "생성된 설문조사 ID", example = "1")
    private Long surveyId;
}