package site.okkul.be.domain.survey.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Q2-2. 최근 어떤 강의를 수강했습니까?
 * 학생의 강의 수강 유형을 나타냅니다.
 */
@Getter
public enum ClassType {
    DEGREE(1, "학위 과정 수업"),
    EXTENSION(2, "전문 기술 향상을 위한 평생 학습"),
    LANGUAGE(3, "어학 수업"),
    FIVE_YEAR(4, "수강 후 5년 이상 지남");

    private final Integer value;
    private final String description;
    private static final Map<Integer, ClassType> VALUE_MAP =
            Arrays.stream(values())
                .collect(Collectors.toMap(ClassType::getValue, Function.identity()));

    ClassType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    // value를 통해 Enum을 찾는 메서드
    public static ClassType fromValue(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)) {
            throw new IllegalArgumentException("잘못된 ClassType value입니다: " + value);
        }
        return VALUE_MAP.get(value);
    }
}
