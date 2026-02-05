package site.okkul.be.domain.topic.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import site.okkul.be.domain.topic.response.CategoryTopicResponse;
import site.okkul.be.global.config.SwaggerConfig;

@Tag(name = SwaggerConfig.TOPIC, description = "토픽 관련 API")
public interface TopicControllerDocs {
    @Operation(
            summary = "4번쟤 질문 토픽 조회 API (설문조사 4번째 질문 화면 구성용)",
            description =
                    "설문지 Part 4 of 4 질문 \"아래의 설문에서 총 12개 이상의 항목을 선택하십시오.\"에 대한 카테고리별 토픽 목록을" +
                            "카테고리별로 그룹화하여 반환합니다. " +
                            "중분류 카테고리가 (1:LEISURE, 2:HOBBY, 3:EXERCISE, 4:HOLIDAY) 인 항목만 가져옵니다."  +
                            "해당 API는 유저에 따라서 달라지지 않습니다." +
                            "프론트엔드는 이 데이터를 받아 화면을 구성합니다.")
    ResponseEntity<CategoryTopicResponse> getSurveyTopics();

}
