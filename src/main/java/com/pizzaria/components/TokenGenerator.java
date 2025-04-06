package com.pizzaria.components;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateVerificationCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("O tamanho do código deve ser maior que zero.");
        }

        return SECURE_RANDOM.ints(length, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    public String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }
}