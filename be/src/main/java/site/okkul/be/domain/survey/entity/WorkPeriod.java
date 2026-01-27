package site.okkul.be.domain.survey.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Q1-4. 귀하의 근무 기간은 얼마나 되십니까?
 * 사용자의 근무 기간을 분류합니다.
 */
@Getter
public enum WorkPeriod {
    LESS2M_WITHOUT_PRIOR(1, "첫 직장 - 2개월 미만"),
    LESS2M_WITH_PRIOR_EXP(2, "교직은 처음이지만 이전에 다른 직업을 가진 적 있음 - 2개월 미만"),
    OVER2M_WITHOUT_PRIOR(3, "첫 직장 - 2개월 이상"),
    MANY_EXP(4, "경력 많음 (첫 직장 아님)");

    private final Integer value;
    private final String description;
    private static final Map<Integer, WorkPeriod> VALUE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(WorkPeriod::getValue, Function.identity()));

    WorkPeriod(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    // value를 통해 Enum을 찾는 메서드
    public static WorkPeriod fromValue(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)) {
            throw new IllegalArgumentException("잘못된 WorkPeriod value입니다: " + value);
        }
        return VALUE_MAP.get(value);
    }
}
