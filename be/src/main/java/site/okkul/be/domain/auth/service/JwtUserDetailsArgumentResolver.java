package site.okkul.be.domain.auth.service;

import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import site.okkul.be.domain.auth.dto.JwtUserDetails;

@Component
public class JwtUserDetailsArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// 1. @AuthUser 어노테이션이 붙어있고
		boolean hasAnnotation = parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
		// 2. 파라미터 타입이 JwtUserDetails (또는 UserDetails) 인지 확인
		boolean isUserType = JwtUserDetails.class.isAssignableFrom(parameter.getParameterType());

		return hasAnnotation && isUserType;
	}

	@Override
	public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		// 1. SecurityContext에서 인증 정보 꺼내기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 2. 인증 정보가 없거나, 타입이 안 맞으면 null 반환 (또는 예외 발생)
		if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserDetails)) {
			return null; // 인증 안 된 상태에서 호출하면 null이 들어감
		}

		// 3. JwtUserDetails 객체 반환 -> 컨트롤러 파라미터에 쏙 들어감
		return authentication.getPrincipal();
	}
}
