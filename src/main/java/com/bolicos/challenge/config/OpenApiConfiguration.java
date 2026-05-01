package com.bolicos.challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI challengeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Communication Preference API")
                .description("API para gerenciamento de preferências de comunicação.")
                .version("v1")
            );
    }
}
