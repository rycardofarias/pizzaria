package com.pizzaria.service.impl;

import com.pizzaria.exception.EmailSendingException;
import com.pizzaria.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
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

import java.io.UnsupportedEncodingException;

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
            log.debug("Preparando email de verificação: {}", to);
            MimeMessage message = createVerificationEmail(to, token);

            mailSender.send(message);
            log.info("Email de verificação enviado: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de verificação: {}", to, e);
            throw new EmailSendingException("Não foi possível enviar o email de verificação");
        }
    }

    private MimeMessage createVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(new InternetAddress(fromEmail, "Pizzaria", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("Erro ao definir remetente personalizado", e);
            helper.setFrom(fromEmail);
        }

        helper.setTo(to);
        helper.setSubject("Confirmação de Email - Pizzaria");

        String verificationUrl = appBaseUrl + "/email/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("userName", to.split("@")[0]);

        String htmlContent = templateEngine.process("email-verification", context);
        helper.setText(htmlContent, true);

        return message;
    }

    @Async
    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            log.debug("Preparando email com código de verificação: {}", to);
            MimeMessage message = createVerificationCodeEmail(to, code);

            mailSender.send(message);
            log.info("Email com código de verificação enviado: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email com código de verificação: {}", to, e);
            throw new EmailSendingException("Não foi possível enviar o email com código de verificação");
        }
    }

    private MimeMessage createVerificationCodeEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(new InternetAddress(fromEmail, "Pizzaria", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("Erro ao definir remetente personalizado", e);
            helper.setFrom(fromEmail);
        }

        helper.setTo(to);
        helper.setSubject("Código de Verificação - Pizzaria");

        Context context = new Context();
        context.setVariable("verificationCode", code);
        context.setVariable("userName", to.split("@")[0]);
        context.setVariable("expiryMinutes", 10);

        String htmlContent = templateEngine.process("email-verification-code", context);
        helper.setText(htmlContent, true);

        return message;
    }
}