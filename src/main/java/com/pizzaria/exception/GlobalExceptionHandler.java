package com.pizzaria.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ExceptionLogger.logWarn(HttpStatus.NOT_FOUND, ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado.", request);
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(org.springframework.security.core.userdetails.UsernameNotFoundException ex, WebRequest request) {
        log.warn("Tentativa de acesso com usuário inexistente: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.", request);
    }

    @ExceptionHandler(com.pizzaria.exception.TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(com.pizzaria.exception.TokenRefreshException ex, WebRequest request) {
        log.warn("Falha ao renovar token: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Falha ao renovar token de acesso.", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Erro inesperado: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ExceptionLogger.logWarn(HttpStatus.BAD_REQUEST, ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex, WebRequest request) {
        ExceptionLogger.logError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao enviar e-mail", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Falha no envio de e-mail: " + ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ExceptionLogger.logValidationErrors(errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos campos", errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        ExceptionLogger.logValidationErrors(errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação nos parâmetros", errors, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        ExceptionLogger.logError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no sistema", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        return buildResponse(status, message, null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        String errorMessage = "Token de atualização já existe para este usuário.";
        ExceptionLogger.logError(HttpStatus.BAD_REQUEST, errorMessage, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Map<String, String> errors, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                request.getDescription(false),
                errors
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}