package site.okkul.be.infra.ai;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import site.okkul.be.infra.ai.dto.AiFeedbackRequest;
import site.okkul.be.infra.ai.dto.AiFeedbackResponse;

/**
 * AI 서버와 통신하기 위한 선언적 HTTP 클라이언트 인터페이스입니다.
 */
public interface AiClient {

    @PostExchange("/v1/analyze")
    AiFeedbackResponse requestFeedback(@RequestBody AiFeedbackRequest request);
}
