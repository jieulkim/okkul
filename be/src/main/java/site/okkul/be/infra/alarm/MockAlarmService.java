package site.okkul.be.infra.alarm;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"local", "default"})
@Service
public class MockAlarmService implements AlarmService {
	@Override
	public void sendErrorAlarm(Exception exception, String requestUrl) {
		// Mock implementation: do nothing or log locally
	}
}
