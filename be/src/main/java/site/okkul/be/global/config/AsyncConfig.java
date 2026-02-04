package site.okkul.be.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import site.okkul.be.global.exception.SystemException;
import site.okkul.be.infra.alarm.AlarmService;

@Slf4j
@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {
	private final AlarmService alarmService;

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) -> {
			log.error("Async Error in method: {}, params: {}", method.getName(), params);

			// 여기서 알람 발송!
			if (ex instanceof SystemException systemException) {
				alarmService.sendMessage("🚨 [비동기] 시스템 장애", systemException.getTitle(), systemException.getMessage());
			}
		};
	}
}
