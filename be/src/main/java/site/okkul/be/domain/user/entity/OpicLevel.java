package site.okkul.be.domain.user.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OPIC 등급을 나타내는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum OpicLevel {
	ADVANCED_LOW("AL"),
	INTERMEDIATE_HIGH("IH"),
	INTERMEDIATE_MID_3("IM3"),
	INTERMEDIATE_MID_2("IM2"),
	INTERMEDIATE_MID_1("IM1"),
	INTERMEDIATE_LOW("IL"),
	NOVICE_HIGH("NH"),
	NOVICE_MID("NM"),
	NOVICE_LOW("NL");

	private final String grade;

	// "AL" 같은 문자열로 Enum을 찾는 메서드 (JSON 파싱이나 DB 조회 시 유용)
	public static OpicLevel fromGrade(String grade) {
		return Arrays.stream(values())
				.filter(level -> level.grade.equals(grade))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 등급입니다: " + grade));
	}
}
