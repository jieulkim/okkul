package site.okkul.be.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackRequest {
    private String question_text;
    private String user_answer;
    private String user_korean_script;
}
