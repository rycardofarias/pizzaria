package com.pizzaria.service.impl;

import com.pizzaria.entity.User;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.ResourceNotFoundException;
import com.pizzaria.repository.UserRepository;
import com.pizzaria.service.EmailService;
import com.pizzaria.components.TokenGenerator;
import com.pizzaria.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public void initiateEmailVerification(User user) {
        String verificationCode = tokenGenerator.generateVerificationCode(6);
        setVerificationToken(user, verificationCode);
        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), verificationCode);
        log.info("Processo de verificação de email iniciado para: {}", user.getEmail());
    }

    @Transactional
    public void initiateEmailChangeVerification(User user) {
        String verificationToken = tokenGenerator.generateUniqueToken();
        setVerificationToken(user, verificationToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getPendingEmail(), verificationToken);
        log.info("Processo de verificação para mudança de email iniciado para: {}", user.getPendingEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Verificando email com token");

        User user = findUserByToken(token);
        validateTokenExpiration(user);
        completeVerification(user);

        log.info("Email verificado com sucesso para usuário: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmailWithCode(String email, String code) {
        log.info("Verificando email com código");

        User user = findUserByEmail(email);
        validateVerificationCode(user, code);
        completeVerification(user);

        log.info("Email verificado com sucesso para usuário: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationCode(String email) {
        log.info("Reenviando código de verificação para: {}", email);

        User user = findUserByEmailOrPendingEmail(email);
        validateNeedsVerification(user);

        String targetEmail = determineTargetEmail(user, email);
        String verificationCode = tokenGenerator.generateVerificationCode(6);

        setVerificationToken(user, verificationCode);
        userRepository.save(user);

        emailService.sendVerificationCode(targetEmail, verificationCode);
        log.info("Código de verificação reenviado para: {}", targetEmail);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Reenviando email de verificação para: {}", email);

        User user = findUserByEmailOrPendingEmail(email);
        validateNeedsVerification(user);

        String targetEmail = determineTargetEmail(user, email);
        String verificationToken = tokenGenerator.generateUniqueToken();

        setVerificationToken(user, verificationToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(targetEmail, verificationToken);
        log.info("Email de verificação reenviado para: {}", targetEmail);
    }

    // Métodos privados auxiliares
    private User findUserByToken(String token) {
        return userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    log.error("Token de verificação de email inválido: {}", token);
                    throw new BadRequestException("Token inválido");
                });
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado: {}", email);
                    throw new ResourceNotFoundException("Usuário não encontrado");
                });
    }

    private User findUserByEmailOrPendingEmail(String email) {
        return userRepository.findByEmailOrPendingEmail(email, email)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com o email: {}", email);
                    throw new ResourceNotFoundException("Usuário não encontrado");
                });
    }

    private void validateTokenExpiration(User user) {
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error("Token de verificação expirado para usuário: {}", user.getEmail());
            throw new BadRequestException("Token expirado");
        }
    }

    private void validateVerificationCode(User user, String code) {
        if (!user.getEmailVerificationToken().equals(code)) {
            log.error("Código de verificação inválido para usuário: {}", user.getEmail());
            throw new BadRequestException("Código de verificação inválido");
        }

        validateTokenExpiration(user);
    }

    private void validateNeedsVerification(User user) {
        if (user.isEmailVerified() && user.getPendingEmail() == null) {
            log.error("Email já verificado para usuário: {}", user.getEmail());
            throw new BadRequestException("Email já verificado");
        }
    }

    private String determineTargetEmail(User user, String requestEmail) {
        return requestEmail.equals(user.getEmail()) ? user.getEmail() : user.getPendingEmail();
    }

    private void setVerificationToken(User user, String token) {
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
    }

    private void completeVerification(User user) {
        if (user.getPendingEmail() != null) {
            log.info("Atualizando email de '{}' para '{}'", user.getEmail(), user.getPendingEmail());
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }
}