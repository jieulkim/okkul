package site.okkul.be.domain.survey.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import site.okkul.be.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SurveyErrorCode implements ErrorCode {

	SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY_001", "설문조사를 찾을 수 없습니다."),
	INVALID_TOPIC_ID(HttpStatus.BAD_REQUEST, "SURVEY_002", "유효하지 않은 토픽 ID가 포함되어 있습니다."),
	INVALID_SURVEY_CLASS_VALUE(HttpStatus.BAD_REQUEST, "SURVEY_003", "잘못된 ClassType value입니다."),
	INVALID_SURVEY_OCCUPATION_VALUE(HttpStatus.BAD_REQUEST, "SURVEY_003", "잘못된 Occupation value입니다."),
	INVALID_SURVEY_RESIDENCE_VALUE(HttpStatus.BAD_REQUEST, "SURVEY_003", "잘못된 Residence value입니다."),
	INVALID_SURVEY_TEACHING_VALUE(HttpStatus.BAD_REQUEST, "SURVEY_003", "잘못된 Teaching value입니다."),
	INVALID_SURVEY_WORK_PERIOD_VALUE(HttpStatus.BAD_REQUEST, "SURVEY_003", "잘못된 Work period value입니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
