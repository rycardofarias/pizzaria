package com.pizzaria.service;

public interface AuthService {
    void auditLogout(String email, String ipAddress);
    void auditRefreshToken(String email, String ipAddress);
    void auditRegister(String email, String ipAddress);
    void loginSuccess(String email, String ipAddress, String deviceFingerprint);
    void validateLoginAttempt(String email, String ipAddress, String ipHeaders, String deviceFingerprint);
    boolean isUserLockedOut(String email);
    void validateRefreshTokenAttempt(String token);
    void refreshTokenSuccess();
}

