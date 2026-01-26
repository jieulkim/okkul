package site.okkul.be.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {

	// 스웨거에서 사용할 인증 방식 이름
	public static final String BEARER_AUTH = "BearerAuth";

	// 태그 이름들
	public static final String AUTH = "Auth";
	public static final String USER = "User";
	public static final String QUESTION = "Question";

	private final SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name(HttpHeaders.AUTHORIZATION);

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(BEARER_AUTH, securityScheme))
				.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
				.title("Swagger API Specifications")
				.description("""
						## Okkul REST API
						
						## Authentication
						[Oauth 로그인 리스트 확인](/login)
						
						로그인 시 자동으로 JWT 토큰 발급 후 FE로 리다이렉트 됩니다.
						
						지금은 개발이 미숙해서? URL에 토큰이 노출되니 주의하세요!
						""")
				.version("1.0.0");
	}
}
