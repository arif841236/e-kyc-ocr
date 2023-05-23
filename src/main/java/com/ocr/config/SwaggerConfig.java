package com.ocr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	OpenAPI customCofiguration() {
		return new OpenAPI()
				.components(new Components())
				.info(new Info().title("Int Origin E-KYC OCR Documentation")
						.description("Api Documentation").version("V1")
						//						.contact(new Contact()
						//								.email("mohammad.arif@indusnet.co.in")
						//								.name("Mohammad Arif")
						//								.url("https://bitbucket.org/Indus_Net/workspace/overview"))
						);
	}
}

