package site.okkul.be.infra.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@Slf4j
@SpringBootTest // 스프링의 모든 설정을 로드하여 실제 어플리케이션처럼 테스트하는 통합 테스트용 어노테이션입니다.
@Tag("integration") // 이 테스트를 'integration' 그룹으로 분류합니다.
@DisplayName("AI Client 통합 테스트")
class AiClientIntegrationTest {

    @Autowired
    private AiClient aiClient; // @Mock이 아닌, 실제 설정으로 생성된 AiClient Bean을 주입받습니다.

    @Test
    @DisplayName("실제 AI 서버에 요청을 보내고 정상적으로 응답 객체를 받는다")
    void requestFeedback_to_real_server() {
        // --- 사전 조건 ---
        // 1. AI 서버가 실행 중이어야 합니다.
        // 2. `application.yml`에 `ai.server.url`이 올바르게 설정되어 있어야 합니다.

        // --- Given (준비) ---
        String questionText = "I'd like to know about the bar you often go to. Where is your favorite bar? What does it look like? Please describe everything about that bar in detail.";
        String koranScript = "제가 가장 자주 가는 단골 바는 집 근처 골목에 숨겨진 '아지트'라는 작은 펍이에요. 이곳은 전체적으로 어두운 조명에 빈티지한 벽돌로 꾸며져 있어서, 들어서자마자 굉장히 아늑하고 편안한 느낌을 줘요. 특히 벽면 가득한 LP판에서 흘러나오는 재즈 음악과 은은한 위스키 향이 어우러져서, 하루의 피로를 풀며 친구들과 진솔한 대화를 나누기에 정말 최고의 장소입니다.";
        String userAnswer = "My favorite place to grab a drink is a cozy, neighborhood bar called \"The Hideout\" located just a few blocks from my house. The interior is dimly lit with vintage brick walls and comfortable leather booths, creating a very warm and inviting atmosphere. It’s the perfect spot to unwind because the music is always at a perfect volume, allowing for great conversations with friends. Would you like me to adjust the vocabulary level or add more specific details to make it sound more like your personal style?";
        // 정확한 필드명(question_text, user_answer)을 사용하여 AiRequest DTO를 생성합니다.
        AiFeedbackRequest request = AiFeedbackRequest.builder()
                .question_text(questionText)
                .user_answer(userAnswer)
                .user_korean_script(koranScript)
                .build();

        log.info("AI 서버에 실제 요청을 보냅니다. Question: {}, Answer: {}", request.getQuestion_text(), request.getUser_answer());

        // --- When & Then (실행 및 검증) ---

        // assertTimeoutPreemptively: 지정된 시간(15초) 내에 실행이 완료되지 않으면 테스트를 즉시 실패시킵니다.
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            // aiClient.requestFeedback(request)를 호출하여 실제 HTTP 요청을 보냅니다.
            AiFeedbackResponse response = aiClient.requestFeedback(request);

            // 응답을 로그로 출력하여 눈으로 확인합니다.
            log.info("AI 서버로부터 실제 응답을 받았습니다. 응답 전문: {}", response);
            if (response != null) {
                log.info("Improved Answer: {}", response.getImproved_answer());
                log.info("피드백 내용 (relevance_feedback): {}", response.getRelevance_feedback());
            }

            // 응답 객체가 null이 아닌지, 대표 피드백 내용(improved_answer)이 비어있지 않은지 검증합니다.
            assertThat(response).isNotNull();
            assertThat(response.getImproved_answer()).isNotNull().isNotEmpty();
            assertThat(response.getRelevance_feedback()).isNotNull().isNotEmpty();

        }, "AI 서버가 15초 내에 응답하지 않았습니다.");
    }
}
