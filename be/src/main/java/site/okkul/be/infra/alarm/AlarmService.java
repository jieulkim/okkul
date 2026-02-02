package site.okkul.be.infra.alarm;

public interface AlarmService {
	void sendErrorAlarm(Exception exception, String requestUrl);
}
