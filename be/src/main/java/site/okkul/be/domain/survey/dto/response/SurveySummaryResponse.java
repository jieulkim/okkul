package site.okkul.be.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "사용자의 설문조사 목록 조회용 요약 정보 응답 DTO")
public class SurveySummaryResponse {
    @Schema(description = "설문조사 ID", example = "1")
    private Long surveyId;

    @Schema(description = "생성일 (UTC)", example = "2024-01-22T12:30:00Z")
    private Instant createdAt;

    @Schema(description = "직군", example = "COMPANY")
    private String occupation;

    @Schema(description = "자가진단 레벨", example = "4")
    private Integer level;

    @Schema(description = "직업 유무", example = "true")
    private Boolean hasJob;

    @Schema(description = "학생 여부", example = "false")
    private Boolean student;

    @Schema(description = "근무 기간", example = "MANY")
    private String workPeriod;

    @Schema(description = "관리직 여부", example = "true")
    private Boolean manager;

    @Schema(description = "거주지 형태", example = "FAMILY")
    private String residence;

    @Schema(description = "소재 목록", example = "[101, 102, 201]")
    private List<Long> topicList;
}