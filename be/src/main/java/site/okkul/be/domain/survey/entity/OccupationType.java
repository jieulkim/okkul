package site.okkul.be.domain.survey.entity;

import lombok.Getter;

/**
 * 설문에서 사용하는 직업 유형을 나타내는 열거형입니다.
 * 각 상수는 사용자 표시용 한국어 설명을 갖습니다.
 *
 * @author 김남주
 */
@Getter
public enum OccupationType {
	/**
	 * 사업/회사
	 */
	COMPANY("사업/회사"),
	/**
	 * 재택근무/재택사업
	 */
	HOME("재택근무/재택사업"),
	/**
	 * 교사/교육자
	 */
	EDUCATION("교사/교육자"),
	/**
	 * 일 경험 없음
	 */
	NONE("일 경험 없음");

	/**
	 * 사용자 표시용 설명
	 */
	private final String description;

	/**
	 * 생성자
	 *
	 * @param description 사용자에게 보여줄 설명문
	 */
	OccupationType(String description) {
		this.description = description;
	}
}
