package com.pizzaria.service.impl;

import com.pizzaria.service.AuthService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lockoutTimes = new ConcurrentHashMap<>();
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Timed
    public void validateLoginAttempt(String email) {
        meterRegistry.counter("auth.login.attempts.total").increment();
        
        if (isUserLockedOut(email)) {
            meterRegistry.counter("auth.login.attempts.locked").increment();
            throw new LockedException("Conta bloqueada. Tente novamente em " + LOCKOUT_DURATION_MINUTES + " minutos");
        }

        AtomicInteger attempts = loginAttempts.computeIfAbsent(email, k -> new AtomicInteger(0));
        if (attempts.incrementAndGet() >= MAX_LOGIN_ATTEMPTS) {
            lockoutTimes.put(email, LocalDateTime.now());
            meterRegistry.counter("auth.login.attempts.maxed").increment();
            throw new LockedException("Número máximo de tentativas excedido. Conta bloqueada por " + LOCKOUT_DURATION_MINUTES + " minutos");
        }
    }

    @Timed
    public void loginSuccess(String email) {
        loginAttempts.remove(email);
        lockoutTimes.remove(email);
        meterRegistry.counter("auth.login.success.total").increment();
    }

    public boolean isUserLockedOut(String email) {
        LocalDateTime lockoutTime = lockoutTimes.get(email);
        if (lockoutTime != null) {
            if (lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                return true;
            } else {
                // Lockout period expired
                loginAttempts.remove(email);
                lockoutTimes.remove(email);
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