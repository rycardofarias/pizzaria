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
import com.pizzaria.repository.IpLoginAttemptRepository;
import com.pizzaria.entity.IpLoginAttempt;
import com.pizzaria.repository.DeviceLoginAttemptRepository;
import com.pizzaria.entity.DeviceLoginAttempt;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    private AuditService auditService;
    @Autowired
    private SecurityEventNotifier securityEventNotifier;
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IpLoginAttemptRepository ipLoginAttemptRepository;
    @Autowired
    private DeviceLoginAttemptRepository deviceLoginAttemptRepository;

    @Value("${auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${auth.max-ip-login-attempts:10}")
    private int maxIpLoginAttempts;

    @Value("${auth.ip-lockout-duration-minutes:30}")
    private int ipLockoutDurationMinutes;

    @Value("${auth.max-device-login-attempts:1}")
    private int maxDeviceLoginAttempts;

    @Value("${auth.device-lockout-duration-minutes:30}")
    private int deviceLockoutDurationMinutes;


    @Timed
    public void validateLoginAttempt(String email, String ipAddress, String ipHeaders, String deviceFingerprint) {
        // --- RATE LIMITING POR IP ---
        if (ipAddress != null && !ipAddress.isBlank()) {
            IpLoginAttempt ipAttempt = ipLoginAttemptRepository.findByIpAddress(ipAddress).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (ipAttempt != null && ipAttempt.getLockoutTime() != null &&
                ipAttempt.getLockoutTime().plusMinutes(ipLockoutDurationMinutes).isAfter(now)) {
                log.warn("[RATE LIMIT] IP BLOQUEADO: {} | Motivo: {} tentativas falhas em {} minutos. Bloqueio até {}", ipAddress, maxIpLoginAttempts, ipLockoutDurationMinutes, ipAttempt.getLockoutTime().plusMinutes(ipLockoutDurationMinutes));
                throw new LockedException("Muitas tentativas deste IP. Tente novamente em " + ipLockoutDurationMinutes + " minutos");
            }
        }
        // --- RATE LIMITING POR DEVICE FINGERPRINT ---
        if (deviceFingerprint != null && !deviceFingerprint.isBlank()) {
            DeviceLoginAttempt deviceAttempt = deviceLoginAttemptRepository.findByDeviceFingerprint(deviceFingerprint).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (deviceAttempt != null && deviceAttempt.getLockoutTime() != null &&
                deviceAttempt.getLockoutTime().plusMinutes(deviceLockoutDurationMinutes).isAfter(now)) {
                log.warn("[RATE LIMIT] DEVICE BLOQUEADO: {} | Motivo: {} tentativas falhas em {} minutos. Bloqueio até {}", deviceFingerprint, maxDeviceLoginAttempts, deviceLockoutDurationMinutes, deviceAttempt.getLockoutTime().plusMinutes(deviceLockoutDurationMinutes));
                throw new LockedException("Muitas tentativas deste dispositivo. Tente novamente em " + deviceLockoutDurationMinutes + " minutos");
            }
        }
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
            log.warn("[RATE LIMIT] USUÁRIO BLOQUEADO: {} | Motivo: {} tentativas falhas em {} minutos. Bloqueio até {}", email, maxLoginAttempts, lockoutDurationMinutes, user.getLockoutTime().plusMinutes(lockoutDurationMinutes));
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

        // --- RATE LIMITING POR IP ---
        if (ipAddress != null && !ipAddress.isBlank()) {
            IpLoginAttempt ipAttempt = ipLoginAttemptRepository.findByIpAddress(ipAddress).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (ipAttempt == null) {
                ipAttempt = new IpLoginAttempt();
                ipAttempt.setIpAddress(ipAddress);
                ipAttempt.setFailedAttempts(1);
                ipAttempt.setLockoutTime(null);
            } else {
                // Limpa lockout se expirou
                if (ipAttempt.getLockoutTime() != null && ipAttempt.getLockoutTime().plusMinutes(ipLockoutDurationMinutes).isBefore(now)) {
                    ipAttempt.setFailedAttempts(0);
                    ipAttempt.setLockoutTime(null);
                }
                ipAttempt.setFailedAttempts(ipAttempt.getFailedAttempts() + 1);
            }
            // Bloqueia IP se excedeu o limite
            if (ipAttempt.getFailedAttempts() >= maxIpLoginAttempts) {
                ipAttempt.setLockoutTime(now);
            }
            ipLoginAttemptRepository.save(ipAttempt);
            if (ipAttempt.getLockoutTime() != null && ipAttempt.getLockoutTime().isEqual(now)) {
                throw new LockedException("Muitas tentativas deste IP. Tente novamente em " + ipLockoutDurationMinutes + " minutos");
            }
        }

        // --- RATE LIMITING POR DEVICE FINGERPRINT ---
        if (deviceFingerprint != null && !deviceFingerprint.isBlank()) {
            DeviceLoginAttempt deviceAttempt = deviceLoginAttemptRepository.findByDeviceFingerprint(deviceFingerprint).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (deviceAttempt == null) {
                deviceAttempt = new DeviceLoginAttempt();
                deviceAttempt.setDeviceFingerprint(deviceFingerprint);
                deviceAttempt.setFailedAttempts(1);
                deviceAttempt.setLockoutTime(null);
            } else {
                // Limpa lockout se expirou
                if (deviceAttempt.getLockoutTime() != null && deviceAttempt.getLockoutTime().plusMinutes(deviceLockoutDurationMinutes).isBefore(now)) {
                    deviceAttempt.setFailedAttempts(0);
                    deviceAttempt.setLockoutTime(null);
                }
                deviceAttempt.setFailedAttempts(deviceAttempt.getFailedAttempts() + 1);
            }
            // Bloqueia device se excedeu o limite
            if (deviceAttempt.getFailedAttempts() >= maxDeviceLoginAttempts) {
                deviceAttempt.setLockoutTime(now);
            }
            deviceLoginAttemptRepository.save(deviceAttempt);
            if (deviceAttempt.getLockoutTime() != null && deviceAttempt.getLockoutTime().isEqual(now)) {
                throw new LockedException("Muitas tentativas deste dispositivo. Tente novamente em " + deviceLockoutDurationMinutes + " minutos");
            }
        }

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
    public void loginSuccess(String email, String ipAddress, String deviceFingerprint) {
        log.debug("[loginSuccess] Chamado para: {}", email);
        log.info("Login bem-sucedido para {}. Resetando tentativas e desbloqueando conta, se necessário.", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
            try {
                auditService.logEvent(
                    email,
                    user.getId() != null ? user.getId().toString() : email,
                    "LOGIN_SUCCESS",
                    "User",
                    user.getId() != null ? user.getId().toString() : null,
                    null,
                    null,
                    ipAddress,
                    "Login successful",
                    "SUCESSO",
                    "Authentication"
                );
            } catch (Exception e) {
                log.error("Erro ao registrar evento de login bem-sucedido na auditoria para {}", email, e);
            }
        }
        meterRegistry.counter("auth.login.success.total").increment();

        // Limpeza de tentativas por IP após login bem-sucedido
        if (ipAddress != null && !ipAddress.isBlank()) {
            IpLoginAttempt ipAttempt = ipLoginAttemptRepository.findByIpAddress(ipAddress).orElse(null);
            if (ipAttempt != null) {
                ipAttempt.setFailedAttempts(0);
                ipAttempt.setLockoutTime(null);
                ipLoginAttemptRepository.save(ipAttempt);
            }
        }
        // Limpeza de tentativas por device após login bem-sucedido
        if (deviceFingerprint != null && !deviceFingerprint.isBlank()) {
            DeviceLoginAttempt deviceAttempt = deviceLoginAttemptRepository.findByDeviceFingerprint(deviceFingerprint).orElse(null);
            if (deviceAttempt != null) {
                deviceAttempt.setFailedAttempts(0);
                deviceAttempt.setLockoutTime(null);
                deviceLoginAttemptRepository.save(deviceAttempt);
            }
        }
    }

    public void auditLogout(String email, String ipAddress) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user = userOpt.orElse(null);
            auditService.logEvent(
                email,
                user != null && user.getId() != null ? user.getId().toString() : email,
                "LOGOUT",
                "User",
                user != null && user.getId() != null ? user.getId().toString() : null,
                null,
                null,
                ipAddress,
                "Logout successful",
                "SUCESSO",
                "Authentication"
            );
        } catch (Exception e) {
            log.error("Erro ao registrar evento de logout na auditoria para {}", email, e);
        }
    }

    public void auditRefreshToken(String email, String ipAddress) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user = userOpt.orElse(null);
            auditService.logEvent(
                email,
                user != null && user.getId() != null ? user.getId().toString() : email,
                "REFRESH_TOKEN",
                "User",
                user != null && user.getId() != null ? user.getId().toString() : null,
                null,
                null,
                ipAddress,
                "Refresh token successful",
                "SUCESSO",
                "Authentication"
            );
        } catch (Exception e) {
            log.error("Erro ao registrar evento de refresh token na auditoria para {}", email, e);
        }
    }

    public void auditRegister(String email, String ipAddress) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user = userOpt.orElse(null);
            auditService.logEvent(
                email,
                user != null && user.getId() != null ? user.getId().toString() : email,
                "REGISTER",
                "User",
                user != null && user.getId() != null ? user.getId().toString() : null,
                null,
                null,
                ipAddress,
                "User registration successful",
                "SUCESSO",
                "Authentication"
            );
        } catch (Exception e) {
            log.error("Erro ao registrar evento de registro na auditoria para {}", email, e);
        }
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