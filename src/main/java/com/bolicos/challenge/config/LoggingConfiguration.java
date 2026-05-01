package com.bolicos.challenge.config;

import com.bolicos.challenge.config.observability.HttpRequestMdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfiguration {

    @Bean
    public HttpRequestMdcFilter httpRequestMdcFilter() {
        return new HttpRequestMdcFilter();
    }
}
