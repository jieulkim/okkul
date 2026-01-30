package site.okkul.be.domain.qustion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
//
//@Entity
//@Getter
//@Builder
//@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
//@AllArgsConstructor
//public class QuestionType {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "type_id")
//	private Long id;
//
//	@Column(name = "type_code", length = 50, nullable = false)
//	private String typeCode; // Intro, Combo2, Combo3, RP1, RP2, RP3, Ad2
//
//	@Column(name = "description", columnDefinition = "TEXT")
//	private String description;
//
//	public void update(String typeCode, String description) {
//		this.typeCode = typeCode;
//		this.description = description;
//	}
//}


@Getter
public enum QuestionType{
	INTRODUCE(1L,"INTRODUCTION","자기소개"),
	COMBO2(2L,"COMBO2","두문제 콤보 - order 1 묘사 > order 2 묘사, 루틴, 비교 중 하나"),
	COMBO3(3L,"COMBO3","세문제 콤보 - order 1 묘사 > order 2 묘사, 루틴, 비교 중 하나, order 3 과거 경험"),
	ROLE_PLAYING1(4L,"RP1","롤플레이 한문제 - 정보 요청"),
	ROLE_PLAYING2(5L,"RP2","롤플레이 두문제 - order 1 묘사 > order 2 이전 질문 관련 eva에게 질문하기"),
	ROLE_PLAYING3(6L,"RP3","롤플레이 세문제 - order 1 정보 요청 > order 2 대안 제시 > order 3 관련 과거 경험"),
	ADVANCED2(7L,"AD2","어드밴스 두문제 - order 1 비교, 묘사, 루틴 > order 2 이전 관련 이슈, 뉴스, 의견 등"),
	;

	private Long id;
	private String typeCode;
	private String description;

	QuestionType(Long id ,String typeCode,String description ) {
		this.id = id;
		this.typeCode = typeCode;
		this.description = description;
	}

	public static QuestionType fromId(Long id) {
		return Arrays.stream(QuestionType.values())
				.filter(it -> it.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 QuestionType이 없습니다: " + id));
	}
}
