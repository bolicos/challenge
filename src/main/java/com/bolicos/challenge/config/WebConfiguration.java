package com.bolicos.challenge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private static final String LOCALHOST = "http://localhost";
    private static final String LOCALHOST_8080 = "http://localhost:8080";
    private static final String LOCALHOST_8085 = "http://localhost:8085";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(LOCALHOST, LOCALHOST_8080, LOCALHOST_8085)
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
            )
            .allowedHeaders(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                "X-Correlation-Id"
            )
            .exposedHeaders("X-Correlation-Id", HttpHeaders.LOCATION)
            .maxAge(3600);
    }
}
