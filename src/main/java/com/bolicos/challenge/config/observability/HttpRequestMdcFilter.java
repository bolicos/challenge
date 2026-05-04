package com.bolicos.challenge.config.observability;

import com.bolicos.challenge.shared.constants.KafkaKeys;
import com.bolicos.challenge.shared.constants.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class HttpRequestMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = Optional
            .ofNullable(request.getHeader(KafkaKeys.CORRELATION_ID_HEADER))
            .filter(value -> !value.isBlank())
            .orElse(UUID.randomUUID().toString());

        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.PATH, request.getRequestURI());
        MDC.put(MdcKeys.METHOD, request.getMethod());
        MDC.put(MdcKeys.SOURCE, MdcKeys.WEB_SOURCE);

        response.setHeader(KafkaKeys.CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
