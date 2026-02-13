package site.okkul.be.domain.exam.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamStatus {
	BEFORE_START("BEFORE_START", "시작 전"),
	IN_PROGRESS("IN_PROGRESS", "진행중"),
	ANALYZING("ANALYZING", "분석중"),
	COMPLETED("COMPLETED", "완료"),
	ANALYZING_FAILED("ANALYZING_FAILED", "분석 실패");

	private final String code;
	private final String displayName;

	ExamStatus(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public static ExamStatus fromCode(String code) {
		for (ExamStatus s : values()) {
			if (s.code.equalsIgnoreCase(code)) return s;
		}
		throw new IllegalArgumentException("Unknown code: " + code);
	}
}
