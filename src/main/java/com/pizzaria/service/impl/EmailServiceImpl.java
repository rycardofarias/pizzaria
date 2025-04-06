package com.pizzaria.service.impl;

import com.pizzaria.components.EmailTemplateFactory;
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
    private final EmailTemplateFactory templateFactory;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.sender-name:Pizzaria}")
    private String senderName;

    @Value("${app.url}")
    private String appBaseUrl;

    @Async
    @Override
    public void sendVerificationEmail(String to, String token) {
        log.debug("Preparando email de verificação: {}", to);
        try {
            MimeMessage message = createEmailFromTemplate(
                    to,
                    "Confirmação de Email - Pizzaria",
                    templateFactory.createVerificationEmailContext(to, token, appBaseUrl)
            );

            mailSender.send(message);
            log.info("Email de verificação enviado: {}", to);
        } catch (MessagingException e) {
            handleEmailSendingError(to, e, "verificação");
        }
    }

    @Async
    @Override
    public void sendVerificationCode(String to, String code) {
        log.debug("Preparando email com código de verificação: {}", to);
        try {
            MimeMessage message = createEmailFromTemplate(
                    to,
                    "Código de Verificação - Pizzaria",
                    templateFactory.createVerificationCodeContext(to, code)
            );

            mailSender.send(message);
            log.info("Email com código de verificação enviado: {}", to);
        } catch (MessagingException e) {
            handleEmailSendingError(to, e, "código de verificação");
        }
    }

    // Métodos privados auxiliares
    private MimeMessage createEmailFromTemplate(String to, String subject, Context context) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        setEmailSender(helper);
        helper.setTo(to);
        helper.setSubject(subject);

        String templateName = context.getVariable("templateName").toString();
        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);

        return message;
    }

    private void setEmailSender(MimeMessageHelper helper) throws MessagingException {
        try {
            helper.setFrom(new InternetAddress(fromEmail, senderName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("Erro ao definir remetente personalizado", e);
            helper.setFrom(fromEmail);
        }
    }

    private void handleEmailSendingError(String to, Exception e, String emailType) {
        log.error("Erro ao enviar email de {}: {}", emailType, to, e);
        throw new EmailSendingException("Não foi possível enviar o email de " + emailType);
    }
}