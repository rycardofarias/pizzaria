package com.pizzaria.service;


import com.pizzaria.entity.User;

public interface EmailVerificationService {
    void initiateEmailVerification(User user);
    void initiateEmailChangeVerification(User user);

    void verifyEmail(String token);

    void verifyEmailWithCode(String email, String code);

    void resendVerificationCode(String email);

    void resendVerificationEmail(String email);

}
