package site.okkul.be.domain.qustion.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import site.okkul.be.domain.qustion.entity.QuestionType;

@Converter // 이 설정을 넣으면 모든 QuestionType 필드에 자동으로 적용됩니다.
public class QuestionTypeConverter implements AttributeConverter<QuestionType, Long> {

	@Override
	public Long convertToDatabaseColumn(QuestionType attribute) {
		// 엔티티 -> DB (Enum의 id값을 저장)
		return (attribute != null) ? attribute.getId() : null;
	}

	@Override
	public QuestionType convertToEntityAttribute(Long dbData) {
		// DB -> 엔티티 (DB의 id값으로 Enum을 찾음)
		return (dbData != null) ? QuestionType.fromId(dbData) : null;
	}
}
