package com.pizzaria.service.impl;

import com.pizzaria.components.SecurityEventNotifier;
import com.pizzaria.enums.SecurityEventType;
import com.pizzaria.service.AuditService;
import com.pizzaria.service.AuthService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.pizzaria.entity.LoginAttempt;
import com.pizzaria.entity.User;
import com.pizzaria.repository.LoginAttemptRepository;
import com.pizzaria.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    private AuditService auditService; // Proteção força bruta
    @Autowired
    private SecurityEventNotifier securityEventNotifier;
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;


    @Timed
    public void validateLoginAttempt(String email, String ipAddress, String ipHeaders) {
        log.info("[DEBUG] validateLoginAttempt chamado para: {}", email);
        meterRegistry.counter("auth.login.attempts.total").increment();

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user = userOpt.orElse(null);

        // Sempre registra a tentativa, mesmo se bloqueado
        LoginAttempt attempt = LoginAttempt.builder()
            .email(email)
            .success(false)
            .ipAddress(ipAddress)
            .ipHeaders(ipHeaders)
            .attemptTime(LocalDateTime.now())
            .user(user)
            .build();
        loginAttemptRepository.save(attempt);

        // Auditoria de tentativa durante bloqueio
        if (user != null && user.getLockoutTime() != null &&
            user.getLockoutTime().plusMinutes(lockoutDurationMinutes).isAfter(LocalDateTime.now())) {
            log.warn("Usuário {} tentou logar, mas a conta está bloqueada até {}.", email, user.getLockoutTime().plusMinutes(lockoutDurationMinutes));
            meterRegistry.counter("auth.login.attempts.locked").increment();
            try {
                auditService.logEvent(
                    email,
                    user.getId() != null ? user.getId().toString() : email,
                    "LOGIN_BLOCKED",
                    "User",
                    user.getId() != null ? user.getId().toString() : null,
                    null,
                    null,
                    ipAddress,
                    "Tentativa de login durante bloqueio",
                    "FALHA",
                    "Authentication"
                );
            } catch (Exception e) {
                log.error("Erro ao registrar tentativa de login bloqueado na auditoria para {}", email, e);
            }
            throw new LockedException("Conta bloqueada. Tente novamente em " + lockoutDurationMinutes + " minutos");
        }

        // Conta tentativas nos últimos X minutos
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        int failedAttempts = loginAttemptRepository.countFailedAttempts(email, since);
        log.info("[DEBUG] Tentativas atuais para {}: {}", email, failedAttempts);

        // Alerta de segurança ao atingir 3 tentativas
        if (failedAttempts == 3) {
            try {
                log.info("Enviando alerta de segurança para o usuário {} após 3 tentativas inválidas.", email);
                securityEventNotifier.notify(
                    SecurityEventType.LOGIN_FAIL,
                    "Usuário: " + email + " realizou 3 tentativas de senha incorreta."
                );
            } catch (Exception e) {
                log.error("Erro ao enviar alerta de segurança após 3 tentativas de login para {}", email, e);
            }
        }

        // Bloqueia usuário se exceder limite
        if (failedAttempts >= maxLoginAttempts && user != null) {
            user.setLockoutTime(LocalDateTime.now());
            user.setFailedAttempts(failedAttempts);
            userRepository.save(user);
            meterRegistry.counter("auth.login.attempts.maxed").increment();
            String details = "Muitas falhas de login para o usuário: " + email + " (" + failedAttempts + " tentativas)";
            try {
                log.info("Enviando email de conta bloqueada para {} e administrador.", email);
                securityEventNotifier.notifyAccountLocked(email, details);
            } catch (Exception e) {
                log.error("Erro ao enviar email de conta bloqueada para {}", email, e);
            }
            try {
                log.info("Registrando evento de auditoria de bloqueio para {}.", email);
                auditService.logEvent(
                    email,
                    user != null && user.getId() != null ? user.getId().toString() : email,
                    "LOGIN_FAIL",
                    "User",
                    user != null && user.getId() != null ? user.getId().toString() : null,
                    null,
                    null,
                    ipAddress,
                    details,
                    "FALHA",
                    "Authentication"
                );
            } catch (Exception e) {
                log.error("Erro ao registrar alerta de múltiplas falhas de login na auditoria para {}", email, e);
            }
            throw new LockedException("Número máximo de tentativas excedido. Conta bloqueada por " + lockoutDurationMinutes + " minutos");
        }
    }
    

    @Timed
    public void loginSuccess(String email, String ipAddress) {
        log.debug("[loginSuccess] Chamado para: {}", email);
        log.info("Login bem-sucedido para {}. Resetando tentativas e desbloqueando conta, se necessário.", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
        }
        meterRegistry.counter("auth.login.success.total").increment();
    }

    public boolean isUserLockedOut(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getLockoutTime() != null && user.getLockoutTime().plusMinutes(lockoutDurationMinutes).isAfter(LocalDateTime.now())) {
                log.debug("Conta do usuário {} está bloqueada até {}.", email, user.getLockoutTime().plusMinutes(lockoutDurationMinutes));
                return true;
            }
        }
        return false;
    }

    @Timed
    public void validateRefreshTokenAttempt(String token) {
        meterRegistry.counter("auth.token.refresh.attempts").increment();
    }

    public void refreshTokenSuccess() {
        meterRegistry.counter("auth.token.refresh.success").increment();
    }
}