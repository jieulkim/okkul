package site.okkul.be.domain.survey.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import site.okkul.be.domain.survey.dto.request.SurveyCreateRequest;
import site.okkul.be.domain.survey.dto.response.*;

@Tag(name = "설문조사", description = "OPIc 서베이 관련 API")
public interface SurveyControllerDocs {

    /**
     * 설문조사 소재 목록 조회 API (인증 불필요)
     * 설문지 Part 4 of 4 질문 "아래의 설문에서 총 12개 이상의 항목을 선택하십시오."에 대한 카테고리별 소재 목록을 조회합니다.
     * DB topic Table에서 중분류 카테고리가 (1:LEISURE, 2:HOBBY, 3:EXERCISE, 4:HOLIDAY) 인 항목만 가져옵니다.
     * @return SurveyTopicsResponseDto
     */
    @Operation(summary = "설문용 카테고리별 소재 목록 조회",
            description = "설문조사 Part 4(취미/여가) 단계에서 선택할 수 있는 모든 소재를 카테고리별로 그룹화하여 반환합니다. " +
                    "프론트엔드는 이 데이터를 받아 화면을 구성합니다.")
    ResponseEntity<SurveyCategoryTopicResponse> getSurveyTopics();

    /**
     * 설문조사 생성 API (인증 필요)
     * 사용자의 질문별 응답항목을 RequestBody로 받아 Survey 테이블에 저장합니다.
     * @param surveyCreateRequest
     * @return
     */
    @Operation(summary = "설문조사 결과 저장",
            description = "사용자가 응답한 설문조사 결과를 DB에 저장합니다. 사용자가 선택한 항목들은 답변ID 혹은 T/F 혹은 소재ID로 요청바디에 담겨옵니다.")
    ResponseEntity<SurveyCreateResponse> createSurvey(
            @RequestBody(description = "사용자 설문 응답 데이터") SurveyCreateRequest surveyCreateRequest
    );


    /**
     * 설문조사 목록 조회 (인증 필요)
     * @return SurveyListResponseDto
     */
    @Operation(summary = "나의 설문조사 목록 조회", description = "현재 로그인한 사용자가 저장한 모든 설문조사의 요약 목록을 조회합니다.")
    ResponseEntity<SurveyListResponse> getSurveyList();

    /**
     * 사용자의 특정 설문조사 결과 조회 API (인증 필요)
     * @param surveyId
     * @return SurveyResponseDto
     */
    @Operation(summary = "특정 설문조사 결과 조회", description = "설문조사 ID를 통해 특정 설문조사의 상세 결과를 조회합니다. 요청된 설문조사가 현재 로그인한 사용자의 것인지 확인합니다.")
    ResponseEntity<SurveyResponse> getSurveyById(
            @Parameter(in = ParameterIn.PATH, name = "surveyId", description = "조회할 설문조사 ID", required = true, example = "1")
            Long surveyId
    );
}
