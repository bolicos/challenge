package com.bolicos.challenge.config.observability;

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

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String PATH_MDC_KEY = "path";
    public static final String METHOD_MDC_KEY = "method";
    public static final String SOURCE_MDC_KEY = "source";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = Optional
            .ofNullable(request.getHeader(CORRELATION_ID_HEADER))
            .filter(value -> !value.isBlank())
            .orElse(UUID.randomUUID().toString());

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(PATH_MDC_KEY, request.getRequestURI());
        MDC.put(METHOD_MDC_KEY, request.getMethod());
        MDC.put(SOURCE_MDC_KEY, "web");

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
