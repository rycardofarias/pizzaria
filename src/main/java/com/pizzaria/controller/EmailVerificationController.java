package com.pizzaria.controller;

import com.pizzaria.dto.request.EmailRequest;
import com.pizzaria.dto.request.EmailVerificationRequest;
import com.pizzaria.dto.response.ApiResponse;
import com.pizzaria.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController extends BaseController {

    private final EmailVerificationService verificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmailByToken(@RequestParam String token) {
        log.info("Recebida solicitação de verificação de email via token: {}", token);
        verificationService.verifyEmail(token);
        return ResponseEntity.ok("Email verificado com sucesso! Você pode fechar esta página e voltar para o aplicativo.");
    }

    @PostMapping(value = "/verify-email-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyEmailByCode(@Valid @RequestBody EmailVerificationRequest request) {
        log.info("Recebida solicitação de verificação de email via código para: {}", request.getEmail());
        verificationService.verifyEmailWithCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new ApiResponse(true, "Email verificado com sucesso."));
    }

    @PostMapping("/resend/{email}")
    public ResponseEntity<Void> resendVerificationEmail(@PathVariable String email) {
        log.info("Recebida solicitação para reenvio de email de verificação: {}", email);
        verificationService.resendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/resend-verification-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resendVerificationCode(@Valid @RequestBody EmailRequest request) {
        log.info("Recebida solicitação para reenvio de código de verificação: {}", request.getEmail());
        verificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Código de verificação reenviado com sucesso."));
    }
} 