package site.okkul.be.infra.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class AiClientConfig {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Bean
    public AiClient realAiClient() {
        // 1. RestClient 생성 (Spring MVC의 현대적인 HTTP 클라이언트)
        RestClient restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .build();

        // 2. RestClientAdapter를 사용하여 팩토리 생성
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        // 3. 인터페이스 구현체 생성
        return factory.createClient(AiClient.class);
    }
}
