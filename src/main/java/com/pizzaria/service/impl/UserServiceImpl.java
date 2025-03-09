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
import com.pizzaria.service.EmailService;
import com.pizzaria.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    @Transactional
    public User createUser(UserCreateRequest request) {
        log.info("Criando novo usuário: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email já cadastrado: {}", request.getEmail());
            throw new BadRequestException("Email já cadastrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER.getValue())
                .orElseThrow(() -> new RuntimeException("Role não encontrada"));
        user.getRoles().add(userRole);

        user.setActive(true);
        user.setEmailVerified(false);

        String verificationToken = generateEmailVerificationToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        log.info("Usuário criado com sucesso. ID: {}", savedUser.getId());
        return savedUser;
    }

    public void verifyEmail(String token) {
        log.info("Verificando email com token");
    
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    log.error("Token de verificação de email inválido: {}", token);
                    throw new BadRequestException("Token inválido");
                });
    
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error("Token de verificação de email expirado para usuário: {}", user.getEmail());
            throw new BadRequestException("Token expirado");
        }
    
        if (user.getPendingEmail() != null) {
            log.info("Atualizando email de '{}' para '{}'", user.getEmail(), user.getPendingEmail());
            
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    
        log.info("Email verificado com sucesso para usuário: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Reenviando email de verificação para: {}", email);
    
        User user = userRepository.findByEmailOrPendingEmail(email, email)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com o email: {}", email);
                    throw new ResourceNotFoundException("Usuário não encontrado");
                });
    
        String targetEmail = email.equals(user.getEmail()) ? user.getEmail() : user.getPendingEmail();
    
        if (user.isEmailVerified() && user.getPendingEmail() == null) {
            log.error("Email já verificado para usuário: {}", email);
            throw new BadRequestException("Email já verificado");
        }
    
        String verificationToken = generateEmailVerificationToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
    
        emailService.sendVerificationEmail(targetEmail, verificationToken);
        log.info("Email de verificação reenviado para: {}", targetEmail);
    }

    public User getUserById(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado");
                });
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        log.info("Atualizando usuário. ID: {}", id);
        User user = getUserById(id);

        String verificationToken = null;

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("Email já está em uso: {}", request.getEmail());
                throw new BadRequestException("Email já está em uso");
            }

            verificationToken = generateEmailVerificationToken();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            
            user.setPendingEmail(request.getEmail());
            user.setEmailVerified(false); 
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        if (verificationToken != null) {
            emailService.sendVerificationEmail(user.getPendingEmail(), verificationToken);
        }

        log.info("Usuário atualizado com sucesso. ID: {}", id);
        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Desativando usuário. ID: {}", id);
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("Usuário desativado com sucesso. ID: {}", id);
    }

    private String generateEmailVerificationToken() {
        return UUID.randomUUID().toString();
    }

}
