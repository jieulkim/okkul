package site.okkul.be.domain.exam.service;

import java.util.List;
import site.okkul.be.domain.exam.exception.ExamErrorCode;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.global.exception.BusinessException;

public class ExamLevelDesign {

	/**
	 * 1~7번 구성
	 * - Lv1~2: INTRO(1) + COMBO2 * 3세트(=6문항) => 총 7문항
	 * - Lv3~6: INTRO(1) + COMBO3 * 2세트(=6문항) => 총 7문항
	 */
	public static List<QuestionType> getFirstLayoutByLevel(int level) {
		if (level >= 3) {
			return List.of(
					QuestionType.INTRODUCE,
					QuestionType.COMBO3,
					QuestionType.COMBO3
			);
		} else {
			return List.of(
					QuestionType.INTRODUCE,
					QuestionType.COMBO2,
					QuestionType.COMBO2,
					QuestionType.COMBO2
			);
		}
	}

	/**
	 * 8번~마지막 구성
	 * - Lv1~2(총 12문항): COMBO2(1세트=2문항) + RP1(1세트=1문항) + RP2(1세트=2문항) => 5문항 (8~12)
	 * - Lv3~4(총 15문항): COMBO3(1세트=3문항) + RP3(1세트=3문항) + RP2(1세트=2문항) => 8문항 (8~15)
	 * - Lv5~6(총 15문항): COMBO3(1세트=3문항) + RP3(1세트=3문항) + AD2(1세트=2문항) => 8문항 (8~15)
	 */
	public static List<QuestionType> getRemainingLayoutByLevel(int level) {
		if (level >= 5) {
			return List.of(
					QuestionType.COMBO3,
					QuestionType.ROLE_PLAYING3,
					QuestionType.ADVANCED2
			);
		} else if (level >= 3) {
			return List.of(
					QuestionType.COMBO2,
					QuestionType.ROLE_PLAYING3,
					QuestionType.ADVANCED2);
		} else {
			return List.of(
					QuestionType.COMBO2,
					QuestionType.ROLE_PLAYING1,
					QuestionType.ADVANCED2
			);
		}
	}

	/**
	 * typeCode에 따른 세트 기대 문항 수 반환
	 */
	public static int expectedCountByTypeCode(QuestionType type) {
		return switch (type) {
			case QuestionType.INTRODUCE, QuestionType.ROLE_PLAYING1 -> 1;
			case QuestionType.COMBO2, QuestionType.ROLE_PLAYING2, QuestionType.ADVANCED2 -> 2;
			case QuestionType.COMBO3, QuestionType.ROLE_PLAYING3 -> 3;
			default -> throw new BusinessException(ExamErrorCode.UNKNOWN_QUESTION_TYPE);
		};
	}
}
