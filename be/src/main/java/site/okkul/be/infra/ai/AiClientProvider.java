package site.okkul.be.infra.ai;

public interface AiClientProvider {
    /**
     * 로직에 필요한 AiClient 구현체를 반환합니다.
     * @param useReal 실제 AI 클라이언트를 사용할지 여부 (dev 환경 등에서 사용)
     * @return 선택된 AiClient 구현체
     */
    AiClient getClient(boolean useReal);
}
