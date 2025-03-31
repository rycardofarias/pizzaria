package com.pizzaria.service;

public interface AuthService {
    void loginSuccess(String email);

    public void validateLoginAttempt(String email);

    boolean isUserLockedOut(String email);

    void validateRefreshTokenAttempt(String token);


    void refreshTokenSuccess();
}

