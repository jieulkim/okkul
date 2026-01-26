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

@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuestionType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "type_id")
	private Long id;

	@Column(name = "type_code", length = 50, nullable = false)
	private String typeCode; // Intro, Combo2, Combo3, RP1, RP2, RP3, Ad2

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	public void update(String typeCode, String description) {
		this.typeCode = typeCode;
		this.description = description;
	}
}
