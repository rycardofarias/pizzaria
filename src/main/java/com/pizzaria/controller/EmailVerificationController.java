package com.pizzaria.controller;

import com.pizzaria.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth/email")
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
} 