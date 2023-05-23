package com.ocr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EndPointCustomConfiguration implements WebMvcConfigurer  {

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix("", HandlerTypePredicate.forAnnotation(RestController.class));
	}

	@Bean
	DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

}