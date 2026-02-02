package site.okkul.be.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Configuration
public class SwaggerConfig {

	// 스웨거에서 사용할 인증 방식 이름
	public static final String BEARER_AUTH = "BearerAuth";

	// 태그 이름들
	public static final String AUTH = "Auth";
	public static final String USER = "User";
	public static final String QUESTION = "Question";
	public static final String PRACTICE = "Practice";
	public static final String SURVEY = "Survey";
	public static final String TOPIC = "Topic";


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
						[Google 소셜 로그인](/oauth2/authorization/google)
						
						로그인 시 자동으로 JWT 토큰 발급 후 FE로 리다이렉트 됩니다.
						
						지금은 개발이 미숙해서? URL에 토큰이 노출되니 주의하세요!
						""")
				.version("1.0.0");
	}

	/**
	 * Swagger UI의 모든 API에 "X-Use-Real-AI" 헤더 입력 필드를 추가하는 설정
	 */
	@Bean
	public OperationCustomizer customGlobalHeaders() {
		return (operation, handlerMethod) -> {
			operation.addParametersItem(
					new HeaderParameter()
							.name("X-Use-Real-AI")
							.description("dev/cicd 환경에서 실제 AI 서버를 사용하려면 'true'로 설정")
							.schema(new io.swagger.v3.oas.models.media.StringSchema()._enum(List.of("true")))
							.required(false)
			);
			return operation;
		};
	}
}
