package site.okkul.be.infra.alarm;

public interface AlarmService {
	void sendErrorAlarm(Exception exception, String requestUrl);

	void sendMessage(String title, String message, String requestUrl);
}
