package com.pizzaria.service;


public interface EmailService {
    void sendVerificationEmail(String email, String verificationToken);
    void sendVerificationCode(String email, String verificationCode);
    void sendAlertEmail(String to, String body);
}
