package com.pizzaria.service;


public interface EmailService {
    void sendVerificationEmail(String email, String verificationToken);
}
