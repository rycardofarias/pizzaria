package com.pizzaria.service.impl;

import com.pizzaria.entity.User;
import com.pizzaria.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserServiceImpl {
    private final UserService userService;

    public Long getUserIdFromUsername(String username) {
        return userService.findByEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
