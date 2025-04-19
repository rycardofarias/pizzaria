package com.pizzaria.components;

import com.pizzaria.enums.SecurityEventType;
import com.pizzaria.service.impl.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SecurityEventNotifier {

    private static final String SECURITY_ALERT_EMAIL = "rccorinthians71@gmail.com";

    @Autowired
    private EmailServiceImpl emailService;

    public void notify(SecurityEventType eventType, String details) {
        log.warn("[SECURITY EVENT] {} - {}", eventType, details);
        try {

            String userEmail = null;
            if (details != null && details.contains("Usuário:")) {
                log.info("[DEBUG] Tentando extrair e-mail do detalhes: {}", details);
                Matcher matcher = Pattern.compile("Usuário:\\s*([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})").matcher(details);
                if (matcher.find()) {
                    userEmail = matcher.group(1);
                    log.info("[DEBUG] E-mail extraído dos detalhes (regex): {}", userEmail);
                } else {
                    log.warn("[DEBUG] Regex não encontrou e-mail nos detalhes: {}", details);
                }
            } else {
                log.warn("[DEBUG] Nenhum e-mail de usuário encontrado nos detalhes: {}", details);
            }
            String subject = "[Alerta de Segurança] Evento crítico: " + eventType;
            String body = "Um evento de segurança foi detectado no sistema:\n\n" +
                    "Tipo: " + eventType + "\n" +
                    "Detalhes: " + details + "\n" +
                    "Data/Hora: " + java.time.LocalDateTime.now() + "\n";

            log.info("[DEBUG] Enviando alerta para administrador: {}", SECURITY_ALERT_EMAIL);
            emailService.sendAlertEmail(SECURITY_ALERT_EMAIL, body);

            if (userEmail != null && userEmail.contains("@")) {
                log.info("[DEBUG] Enviando alerta para usuário: {}", userEmail);
                emailService.sendAlertEmail(userEmail, body);
            } else {
                log.warn("[DEBUG] Alerta NÃO enviado para usuário porque e-mail não foi identificado.");
            }
        } catch (Exception e) {
            log.error("Erro ao enviar alerta de segurança por email", e);
        }
    }

    public void notifyAccountLocked(String email, String details) {
        log.warn("[SECURITY EVENT] CONTA BLOQUEADA - {}", details);
        try {
            String body = "Sua conta foi bloqueada por múltiplas tentativas de senha incorreta.\n" + details +
                "\nData/Hora: " + java.time.LocalDateTime.now() + "\n";
            log.info("[DEBUG] Enviando email de conta bloqueada para: {}", email);
            emailService.sendAccountLockedEmail(email, body);
            log.info("[DEBUG] Enviando email de conta bloqueada para: {}", SECURITY_ALERT_EMAIL);
            emailService.sendAccountLockedEmail(SECURITY_ALERT_EMAIL, body);
        } catch (Exception e) {
            log.error("Erro ao enviar email de conta bloqueada", e);
        }
    }
}
