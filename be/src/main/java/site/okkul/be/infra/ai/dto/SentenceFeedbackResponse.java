package site.okkul.be.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceFeedbackResponse {

    private String target_sentence;
    private String target_text;
    private String improved_text;
    private String feedback;
    private Integer sentence_order;
}
