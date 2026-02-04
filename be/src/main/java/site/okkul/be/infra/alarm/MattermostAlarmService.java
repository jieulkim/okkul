package site.okkul.be.infra.alarm;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"test", "develop", "production"})
public class MattermostAlarmService implements AlarmService {

	@Value("${notification.mattermost.webhook-url}")
	private String webhookUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	@Async
	@Override
	public void sendErrorAlarm(Exception e, String requestUrl) {
		try {
			Map<String, Object> payload = new HashMap<>();
			String message = String.format("### 🚨 에러 발생\n- **URL**: %s\n- **Exception**: %s\n- **Message**: %s",
					requestUrl, e.getClass().getSimpleName(), e.getMessage());

			payload.put("text", message);
			restTemplate.postForEntity(webhookUrl, payload, String.class);
		} catch (Exception ex) {
			log.error("Mattermost 알림 전송 실패", ex);
		}
	}

	@Override
	public void sendMessage(String title, String message, String requestUrl) {
		try {
			Map<String, Object> payload = new HashMap<>();
			String formattedMessage = String.format("### %s\n%s\n- **URL**: %s", title, message, requestUrl);

			payload.put("text", formattedMessage);
			restTemplate.postForEntity(webhookUrl, payload, String.class);
		} catch (Exception ex) {
			log.error("Mattermost 알림 전송 실패", ex);
		}
	}
}
