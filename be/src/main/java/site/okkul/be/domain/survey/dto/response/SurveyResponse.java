package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 설문조사 결과 조회 응답 DTO")
public class SurveyResponse {
    @Schema(description = "설문조사 ID", example = "1")
    private Long surveyId;
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    @Schema(description = "생성일 (UTC)", example = "2024-01-22T12:30:00Z")
    private Instant createdAt;
    private String occupation;
    private boolean hasJob;
    private String workPeriod;
    private String teachAt;
    private boolean manager;
    private boolean student;
    private String classType;
    private String residence;
    private int level;
    private List<SelectedTopic> selectedTopics;

}