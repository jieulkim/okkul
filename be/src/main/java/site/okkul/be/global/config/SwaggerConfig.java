package site.okkul.be.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.components(new Components())
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
