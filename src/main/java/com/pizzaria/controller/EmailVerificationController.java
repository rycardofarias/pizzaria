package com.pizzaria.controller;

import com.pizzaria.dto.request.EmailRequest;
import com.pizzaria.dto.request.EmailVerificationRequest;
import com.pizzaria.dto.response.ApiResponse;
import com.pizzaria.service.UserService;
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
public class EmailVerificationController {

    private final UserService userService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmailByGet(@RequestParam String token) {
        log.info("Recebida solicitação de verificação de email via GET com token: {}", token);
        
        userService.verifyEmail(token);
        
        return ResponseEntity.ok("Email verificado com sucesso! Você pode fechar esta página e voltar para o aplicativo.");
    }

    @PostMapping("/resend/{email}")
    public ResponseEntity<Void> resendVerificationEmail(@PathVariable String email) {
        log.info("Recebida solicitação para reenvio de email de verificação: {}", email);
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/verify-email-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        userService.verifyEmailWithCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new ApiResponse(true, "Email verificado com sucesso."));
    }

    @PostMapping(value = "/resend-verification-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resendVerificationCode(@Valid @RequestBody EmailRequest request) {
        userService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Código de verificação reenviado com sucesso."));
    }
} 