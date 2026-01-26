package site.okkul.be.domain.survey.entity;

import lombok.Getter;

/**
 * 직장인 선택 시 나타나는 세부 항목입니다.
 *
 * <p>각 상수는 화면에 표시할 설명 문자열을 가집니다.</p>
 *
 * @author 김남주
 */
@Getter
public enum WorkPeriod {
	/**
	 * 첫 직장 - 2개월 미만
	 */
	LESS2M("첫 직장 - 2개월 미만"),

	/**
	 * 첫 직장 - 2개월 이상
	 */
	OVER2M("첫 직장 - 2개월 이상"),

	/**
	 * 경력 많음 (첫 직장 아님)
	 */
	MANY("경력 많음 (첫 직장 아님)");

	/**
	 * 화면에 표시할 설명 문자열
	 */
	private final String description;

	/**
	 * 생성자
	 *
	 * @param description 화면에 표시할 설명
	 */
	WorkPeriod(String description) {
		this.description = description;
	}
}
