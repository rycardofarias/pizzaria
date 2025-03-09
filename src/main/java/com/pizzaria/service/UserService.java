package com.pizzaria.service;

import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.request.UserUpdateRequest;
import com.pizzaria.entity.User;
import jakarta.validation.Valid;

public interface UserService {

    User createUser(@Valid UserCreateRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
    
    User getUserById(Long id);

    User updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

}
