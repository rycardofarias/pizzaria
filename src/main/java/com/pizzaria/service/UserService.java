package com.pizzaria.service;

import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.request.UserUpdateRequest;
import com.pizzaria.entity.User;
import jakarta.validation.Valid;

import java.util.Optional;

public interface UserService {

    User createUser(@Valid UserCreateRequest request);
    
    User getUserById(Long id);

    User updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    Optional<User> findByEmail(String email);

    User saveUser(User user);
}
