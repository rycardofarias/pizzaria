package com.pizzaria.service;

import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.request.UserUpdateRequest;
import com.pizzaria.entity.User;
import jakarta.validation.Valid;

import java.util.Optional;

public interface UserService {

    User createUser(@Valid UserCreateRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
    
    User getUserById(Long id);

    User updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    Optional<User> findByEmail(String email);

    void verifyEmailWithCode(String email, String code);

    void resendVerificationCode(String email);
}
