package com.pizzaria.service;

public interface AuthService {
    void loginSuccess(String email, String ipAddress);

    public void validateLoginAttempt(String email, String ipAddress, String ipHeaders);

    boolean isUserLockedOut(String email);

    void validateRefreshTokenAttempt(String token);


    void refreshTokenSuccess();
}

