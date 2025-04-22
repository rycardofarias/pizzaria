package com.pizzaria.controller;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import java.util.Locale;

public abstract class BaseController {

    @Autowired
    private MessageSource messageSource;

    protected ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String message, String path) {
        return buildErrorResponse(status, message, path, Locale.getDefault());
    }

    protected ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String message, String path, Locale locale) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        String resolvedMessage;
        try {
            resolvedMessage = messageSource.getMessage(message, null, locale);
        } catch (Exception e) {
            resolvedMessage = message;
        }
        body.put("message", resolvedMessage);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}

