package site.okkul.be.domain.survey.entity;

import lombok.Getter;

/**
 * Part 2 학생 여부와 관련된 강의 수강 이력입니다.
 *
 * @author 김남주
 */
@Getter
public enum ClassType {
	DEGREE("학위 과정 수업"),
	EXTENSION("전문 기술 향상을 위한 평생 학습"),
	LANGUAGE("어학 수업"),
	FIVE_YEAR("수강 후 5년 이상 지남");

	private final String description;

	ClassType(String description) {
		this.description = description;
	}
}
