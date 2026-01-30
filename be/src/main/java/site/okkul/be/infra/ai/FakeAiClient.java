package site.okkul.be.infra.ai;

import org.springframework.stereotype.Component;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;
import site.okkul.be.infra.ai.dto.SentenceFeedbackResponse;

import java.util.Collections;

@Component
public class FakeAiClient implements AiClient {
    @Override
    public AiFeedbackResponse requestFeedback(AiFeedbackRequest request) {
        // Return a predefined mock response for testing purposes
        return AiFeedbackResponse.builder()
                .improved_answer("This is a mock improved answer from the Fake AI client.")
                .relevance_feedback("Mock feedback on relevance: Your answer was very relevant.")
                .logic_feedback("Mock feedback on logic: The logic of your answer is sound.")
                .fluency_feedback("Mock feedback on fluency: You speak very fluently.")
                .sentence_details(Collections.singletonList(
                        SentenceFeedbackResponse.builder()
                                .target_sentence("This was the original sentence.")
                                .target_text("original")
                                .improved_text("improved")
                                .feedback("This is a mock feedback on a specific sentence part.")
                                .sentence_order(1)
                                .build()
                ))
                .build();
    }
}
