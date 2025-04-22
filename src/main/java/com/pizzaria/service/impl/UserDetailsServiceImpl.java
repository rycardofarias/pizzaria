package com.pizzaria.service.impl;

import com.pizzaria.entity.User;
import com.pizzaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Tentando carregar usuário por email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Usuário não encontrado com o email: {}", email);
                return new UsernameNotFoundException("Usuário ou senha inválidos.");
            });

        log.debug("Usuário encontrado: {}", user.getEmail());
        return UserDetailsImpl.build(user);
    }

    public UserDetails loadUserById(Long id) {
        log.debug("Tentando carregar usuário por ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Usuário não encontrado com o ID: {}", id);
                return new UsernameNotFoundException("Usuário ou senha inválidos.");
            });

        log.debug("Usuário encontrado: {}", user.getEmail());
        return UserDetailsImpl.build(user);
    }
}