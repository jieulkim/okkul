package site.okkul.be.infra.ai.dto.exam;

import com.fasterxml.jackson.annotation.JsonProperty;

// 문장별 상세 피드백 (ElementCollection 매핑용)
public record SentenceFeedbackDto(
		@JsonProperty("target_sentence")
		String targetSentence,

		@JsonProperty("target_text")
		String targetSegment,

		@JsonProperty("improved_text")
		String correctedSegment,

		@JsonProperty("feedback")
		String comment,

		@JsonProperty("sentence_order")
		Integer sentenceOrder
) {}
