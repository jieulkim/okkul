package site.okkul.be.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackResponse {
    private String improved_answer;
    private String relevance_feedback;
    private String logic_feedback;
    private String fluency_feedback;
    private List<SentenceFeedbackResponse> sentence_details;
}
