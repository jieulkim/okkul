package site.okkul.be.domain.survey.entity;

import lombok.Getter;
import site.okkul.be.domain.survey.exception.SurveyErrorCode;
import site.okkul.be.global.exception.BusinessException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Q3-1. 현재 당신은 어디에 살고 계십니까?
 * 사용자의 거주 형태를 나타냅니다.
 */
@Getter
public enum ResidenceType {
    ALONE(1, "개인 주택이나 아파트에 홀로 거주"),
    FRIENDS(2, "친구/룸메이트와 함께 거주"),
    FAMILY(3, "가족과 함께 거주"),
    DORMITORY(4, "학교 기숙사"),
    MILITARY(5, "군대 막사");

    private final Integer value;
    private final String description;
    private static final Map<Integer, ResidenceType> VALUE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ResidenceType::getValue, Function.identity()));

    ResidenceType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    // value를 통해 Enum을 찾는 메서드
    public static ResidenceType fromValue(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)) {
            throw new BusinessException(SurveyErrorCode.INVALID_SURVEY_RESIDENCE_VALUE);
        }
        return VALUE_MAP.get(value);
    }
}
