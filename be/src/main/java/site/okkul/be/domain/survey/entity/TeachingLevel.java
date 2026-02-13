package site.okkul.be.domain.survey.entity;

import lombok.Getter;
import site.okkul.be.domain.survey.exception.SurveyErrorCode;
import site.okkul.be.global.exception.BusinessException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Q1-2. 현재 귀하는 어디에서 학생을 가르치십니까?
 * 교사/교육자의 경우 학생 강의 장소를 나타냅니다.
 */
@Getter
public enum TeachingLevel {
    UNIVERSITY(1, "대학 이상"),
    SCHOOL(2, "초등/중고등학교"),
    LIFELONG(3, "평생 교육");

    private final Integer value;
    private final String description;
    private static final Map<Integer, TeachingLevel> VALUE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(TeachingLevel::getValue, Function.identity()));

    TeachingLevel(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    // value를 통해 Enum을 찾는 메서드
    public static TeachingLevel fromValue(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)) {
            throw new BusinessException(SurveyErrorCode.INVALID_SURVEY_TEACHING_VALUE);
        }
        return VALUE_MAP.get(value);
    }
}
