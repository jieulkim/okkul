package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사용자의 설문조사 리스트 응답 DTO")
public class SurveyListResponse {
    List<SurveySummaryResponse> surveySummaryResponses;
}
