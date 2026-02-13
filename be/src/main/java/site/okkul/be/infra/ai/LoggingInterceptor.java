package site.okkul.be.infra.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public @NonNull ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body, ClientHttpRequestExecution execution) throws IOException {
		// 1. 요청 로깅
		System.out.println("=== [Request] ===");
		System.out.println("URI    : " + request.getURI());
		System.out.println("Method : " + request.getMethod());
		System.out.println("Headers: " + request.getHeaders());
		System.out.println("Body   : " + new String(body, StandardCharsets.UTF_8));

		// 2. 실행 및 응답 수신
		ClientHttpResponse response = execution.execute(request, body);

		// 3. 응답 로깅
		System.out.println("=== [Response] ===");
		System.out.println("Status : " + response.getStatusCode());
		// BufferingClientHttpRequestFactory 덕분에 여기서 body를 읽어도 실제 클라이언트가 또 읽을 수 있습니다.
		String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
		System.out.println("Body   : " + responseBody);

		return response;
	}
}
