package site.okkul.be.domain.survey.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Q1-1. 현재 귀하는 어느 분야에 종사하고 계십니까?
 * 사용자의 직업 유형을 나타냅니다.
 */
@Getter
public enum OccupationType {
    COMPANY(1, "사업/회사"),
    HOME(2, "재택근무/재택사업"),
    EDUCATION(3, "교사/교육자"),
    NONE(4, "일 경험 없음");

    private final Integer value;
    private final String description;
    private static final Map<Integer, OccupationType> VALUE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(OccupationType::getValue, Function.identity()));

    OccupationType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    // value를 통해 Enum을 찾는 메서드
    public static OccupationType fromValue(Integer value) {
        if (value == null || value == 0) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)) {
            throw new IllegalArgumentException("잘못된 OccupationType value입니다: " + value);
        }
        return VALUE_MAP.get(value);
    }
}
