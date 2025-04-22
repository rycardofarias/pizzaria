package com.pizzaria.service.impl;

import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.request.UserUpdateRequest;
import com.pizzaria.entity.Role;
import com.pizzaria.entity.User;
import com.pizzaria.enums.RoleEnum;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.ResourceNotFoundException;
import com.pizzaria.repository.RoleRepository;
import com.pizzaria.repository.UserRepository;
import com.pizzaria.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationServiceImpl verificationService;

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        log.info("Criando novo usuário: {}", request.getEmail());
        validateEmailNotExists(request.getEmail());

        User user = buildUserFromRequest(request);
        User savedUser = userRepository.save(user);

        verificationService.initiateEmailVerification(savedUser);

        log.info("Usuário criado com sucesso. ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        log.info("Atualizando usuário. ID: {}", id);
        User user = getUserById(id);

        boolean emailChanged = updateUserFields(user, request);
        User updatedUser = userRepository.save(user);

        if (emailChanged) {
            verificationService.initiateEmailChangeVerification(user);
        }

        log.info("Usuário atualizado com sucesso. ID: {}", id);
        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Desativando usuário. ID: {}", id);
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("Usuário desativado com sucesso. ID: {}", id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Métodos privados auxiliares
    private User buildUserFromRequest(UserCreateRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setEmailVerified(false);

        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER.getValue())
                .orElseThrow(() -> new RuntimeException("Role não encontrada"));
        user.getRoles().add(userRole);

        return user;
    }

    private boolean updateUserFields(User user, UserUpdateRequest request) {
        boolean emailChanged = false;

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailNotExists(request.getEmail());
            user.setPendingEmail(request.getEmail());
            user.setEmailVerified(false);
            emailChanged = true;
        }

        return emailChanged;
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Email já cadastrado: {}", email);
            throw new BadRequestException("Não foi possível concluir a solicitação.");
        }
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
