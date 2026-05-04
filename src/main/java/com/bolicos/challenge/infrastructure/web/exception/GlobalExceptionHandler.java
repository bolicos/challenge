package com.bolicos.challenge.infrastructure.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import com.bolicos.challenge.domain.exception.PreferenceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final int NOT_FOUND_CODE = HttpStatus.NOT_FOUND.value();
    public static String NOT_FOUND = HttpStatus.NOT_FOUND.getReasonPhrase();

    public static final int BAD_REQUEST_CODE = HttpStatus.BAD_REQUEST.value();
    public static String BAD_REQUEST = HttpStatus.BAD_REQUEST.getReasonPhrase();

    public static final int INTERNAL_SERVER_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static String INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiError handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        var message = "Recurso não encontrado";
        var details = List.of(ex.getMessage());
        var uri = request.getRequestURI();

        log.warn("{}: method={}, path={}, message={}", message, request.getMethod(), uri, ex.getMessage());

        return ApiError.of(NOT_FOUND_CODE, NOT_FOUND, message, uri, details);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PreferenceNotFoundException.class)
    public ApiError handlePreferenceNotFound(PreferenceNotFoundException ex, HttpServletRequest request) {
        var message = "Recurso não encontrado";
        var details = List.of(ex.getMessage());
        var uri = request.getRequestURI();

        log.warn("{}: method={}, path={}, message={}", message, request.getMethod(), uri, ex.getMessage());

        return ApiError.of(NOT_FOUND_CODE, NOT_FOUND, message, uri, details);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var message = "Payload inválido";
        var uri = request.getRequestURI();
        var details = ex.getBindingResult().getFieldErrors().stream().map(this::formatFieldError).toList();

        log.warn("{}: method={}, path={}, details={}", message, request.getMethod(), uri, details);

        return ApiError.of(BAD_REQUEST_CODE, BAD_REQUEST, message, uri, details);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        var message = "Falha de validação";
        var uri = request.getRequestURI();
        var details = ex.getConstraintViolations().stream().map(this::formatConstraintViolation).toList();

        log.warn("{}: method={}, path={}, details={}", message, request.getMethod(), uri, details);

        return ApiError.of(BAD_REQUEST_CODE, BAD_REQUEST, message, uri, details);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiError handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        var message = "JSON malformado ou tipo inválido";
        var uri = request.getRequestURI();
        var details = List.of(ex.getMessage());

        log.warn("{}: method={}, path={}, message={}", message, request.getMethod(), uri, ex.getMessage());

        return ApiError.of(BAD_REQUEST_CODE, BAD_REQUEST, message, uri, details);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleGeneric(Exception ex, HttpServletRequest request) {
        var message = "Erro inesperado";
        var uri = request.getRequestURI();
        var details = List.of(ex.getMessage());

        log.error(
            "{}: method={}, path={}, exceptionClass={}",
            message,
            request.getMethod(),
            uri,
            ex.getClass().getName(),
            ex
        );

        return ApiError.of(INTERNAL_SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR, message, uri, details);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    public record ApiError(
        String timestamp, int status, String error, String message, String path, List<String> details
    ) {
        static ApiError of(int status, String error, String message, String path, List<String> details) {
            return new ApiError(OffsetDateTime.now().toString(), status, error, message, path, details);
        }
    }
}
