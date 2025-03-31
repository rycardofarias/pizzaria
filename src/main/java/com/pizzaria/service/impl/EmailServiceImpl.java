package com.pizzaria.service.impl;

import com.pizzaria.exception.EmailSendingException;
import com.pizzaria.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appBaseUrl;

    @Async
    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            // Preparar a mensagem
            MimeMessage message = createVerificationEmail(to, token);
            
            // Enviar email
            mailSender.send(message);
            
            log.info("Email de verificação enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de verificação para: {}", to, e);
            throw new EmailSendingException("Não foi possível enviar o email de verificação");
        }
    }

    private MimeMessage createVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        Context context = new Context();
        context.setVariable("verificationToken", token);
        context.setVariable("verificationUrl", appBaseUrl + "/email/verify-email?token=" + token);
        
        String emailContent = templateEngine.process("email-verification", context);
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Confirmação de Email");
        helper.setText(emailContent, true);
        
        return message;
    }

} 