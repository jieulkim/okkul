package site.okkul.be.infra.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.springframework.context.annotation.Profile;

@Component
@Profile("!prod") // "prod" 프로필이 아닐 때 활성화
@RequiredArgsConstructor
public class DevAiClientProvider implements AiClientProvider {

    @Qualifier("realAiClient")
    private final AiClient realAiClient;
    private final FakeAiClient fakeAiClient;

    /**
     * useReal 플래그 값에 따라 실제 AI 클라이언트 또는 가짜 AI 클라이언트를 반환합니다.
     * @param useReal 실제 AI 클라이언트를 사용할지 여부
     * @return 선택된 AiClient 구현체
     */
    @Override
    public AiClient getClient(boolean useReal) {
        if (useReal) {
            return realAiClient;
        }
        return fakeAiClient;
    }
}
