package site.okkul.be.infra.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod") // "prod" 프로필에서 활성화
@RequiredArgsConstructor
public class ProdAiClientProvider implements AiClientProvider {

    @Qualifier("realAiClient")
    private final AiClient realAiClient;

    /**
     * "prod" 환경에서는 파라미터와 상관없이 항상 실제 AI 클라이언트를 반환합니다.
     * @param useReal 이 파라미터는 무시됩니다.
     * @return 항상 realAiClient
     */
    @Override
    public AiClient getClient(boolean useReal) {
        return realAiClient;
    }
}
