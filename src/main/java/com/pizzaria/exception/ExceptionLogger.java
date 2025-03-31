package com.pizzaria.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Slf4j
public class ExceptionLogger {

    public static void logWarn(HttpStatus status, String message) {
        log.warn("[{}] - {}", status, message);
    }

    public static void logError(HttpStatus status, String message, Exception ex) {
        log.error("[{}] - {}: {}", status, message, ex.getMessage(), ex);
    }

    public static void logValidationErrors(Map<String, String> errors) {
        log.warn("[400] - Erro de validação: {}", errors);
    }
}
