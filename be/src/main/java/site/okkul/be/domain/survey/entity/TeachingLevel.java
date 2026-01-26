package site.okkul.be.domain.survey.entity;

import lombok.Getter;

/**
 * 교육 종사자용 추가 설문입니다.
 *
 * @author 김남주
 */
@Getter
public enum TeachingLevel {
	UNIVERSITY("대학 이상"),
	SCHOOL("초등/중고등학교"),
	LIFELONG("평생 교육");

	private final String description;

	TeachingLevel(String description) {
		this.description = description;
	}
}
