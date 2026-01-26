package site.okkul.be.domain.survey.entity;

import lombok.Getter;

/**
 * "숙소/거주 정보" ENUM입니다.
 *
 * @author 김남주
 */
@Getter
public enum ResidenceType {
	ALONE("개인 주택이나 아파트에 홀로 거주"),
	FRIENDS("친구/룸메이트와 함께 거주"),
	FAMILY("가족과 함께 거주"),
	DORMITORY("학교 기숙사"),
	MILITARY("군대 막사");

	private final String description;

	ResidenceType(String description) {
		this.description = description;
	}
}
