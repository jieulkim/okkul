package site.okkul.be.domain.survey.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.dto.response.SurveyCreateResponse;
import site.okkul.be.domain.survey.dto.response.SurveyListResponse;
import site.okkul.be.domain.survey.dto.response.SurveyResponse;
import site.okkul.be.domain.topic.response.SelectedTopics;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.SURVEY, description = "설문조사 관련 API")
public interface SurveyControllerDocs {

    @Operation(
            summary = "설문조사 생성 API",
            description =
                    "사용자의 질문별 응답항목을 RequestBody로 받아 Survey 테이블에 저장합니다. " +
                    "사용자가 선택한 항목들은 답변ID 혹은 T/F 혹은 소재ID로 요청바디에 담겨옵니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<SurveyCreateResponse> createSurvey(
            @RequestBody(description = "사용자 설문 응답 데이터") SurveyCreateRequest surveyCreateRequest,
            @Parameter(hidden = true) UserDetails userDetails
    );

    @Operation(
            summary = "설문조사 목록 조회 API",
            description = "현재 로그인한 사용자가 저장한 모든 설문조사의 요약 목록을 조회합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<SurveyListResponse> getSurveyList(@Parameter(hidden = true) UserDetails userDetails);

    @Operation(
            summary = "특정 설문조사 결과 조회 API",
            description =
                    "설문조사 ID를 통해 특정 설문조사의 상세 결과를 조회합니다. " +
                    "요청된 설문조사가 현재 로그인한 사용자의 것인지 확인합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<SurveyResponse> getSurveyById(
            @Parameter(in = ParameterIn.PATH, name = "surveyId", description = "조회할 설문조사 ID", required = true, example = "1")
            Long surveyId,
            @Parameter(hidden = true) UserDetails userDetails
    );


    @Operation(
            summary = "설문조사 토픽 조회 API (유형별 연습 모드 토픽선택 화면 구성용)",
            description =
                    "설문조사 ID를 통해 특정 설문조사가 포함하고 있는 토픽 목록을 반환합니다." +
                    "중분류 카테고리 상관없이 모든 토픽 목록을 가져옵니다(직업정보, 학생여부, 거주형태 등 포함)" +
                    "유형별 연습 모드에서 특정 설문조사에 따라 선택할 수 있는 토픽을 조회하기 위한 API입니다." +
                    "해당 API는 유저 정보와 설문조사 ID에 따라서 응답이 달라집니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<SelectedTopics> getSurveyTopicById(
            @Parameter(in = ParameterIn.PATH, name = "surveyId", description = "조회할 설문조사 ID", required = true, example = "1")
            Long surveyId,
            @Parameter(hidden = true) UserDetails userDetails
    );
}
