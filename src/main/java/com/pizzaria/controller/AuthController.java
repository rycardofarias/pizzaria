package com.pizzaria.controller;

import com.pizzaria.dto.request.LoginRequest;
import com.pizzaria.dto.request.TokenRefreshRequest;
import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.response.JwtResponse;
import com.pizzaria.dto.response.TokenRefreshResponse;
import com.pizzaria.dto.response.UserResponse;
import com.pizzaria.entity.RefreshToken;
import com.pizzaria.entity.User;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.TokenRefreshException;
import com.pizzaria.security.JwtTokenProvider;
import com.pizzaria.service.AuthService;
import com.pizzaria.service.RefreshTokenService;
import com.pizzaria.service.UserService;
import com.pizzaria.service.impl.UserDetailsImpl;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @PostMapping("/login")
    @Timed
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativa de login para o usuário: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (!userDetails.isEmailVerified()) {
                log.warn("Usuário {} tentou fazer login, mas o email não está verificado.", request.getEmail());
                throw new BadRequestException("Email não verificado. Por favor, verifique seu email.");
            }

            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser()).getToken();

            authService.loginSuccess(request.getEmail());
            log.info("Login realizado com sucesso para usuário: {}", request.getEmail());

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken));

        } catch (BadCredentialsException e) {
            log.warn("Credenciais inválidas para o usuário: {}", request.getEmail());
            throw new BadRequestException("Credenciais inválidas");
        } catch (Exception e) {
            log.error("Erro inesperado ao tentar autenticar usuário {}: {}", request.getEmail(), e.getMessage());
            throw new BadRequestException("Erro ao processar a requisição de login.");
        }
    }

    @PostMapping("/refresh")
    @Timed(value = "auth.refresh.endpoint", description = "Time taken to process token refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Tentativa de refresh token");

        try {
            authService.validateRefreshTokenAttempt(request.getRefreshToken());

            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            String token = tokenProvider.generateTokenFromUser(user);

            authService.refreshTokenSuccess();
            log.info("Token renovado com sucesso para usuário: {}", user.getEmail());

            return ResponseEntity.ok(new TokenRefreshResponse(token, request.getRefreshToken()));
        } catch (TokenRefreshException e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Tentativa de logout");

        String jwt = token.substring(7);
        String username = tokenProvider.getUsernameFromJWT(jwt);
        User user = userService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        refreshTokenService.deleteByUserId(user.getId());

        log.info("Logout realizado com sucesso");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        log.info("Tentativa de registro para usuário: {}", request.getEmail());

        User user = userService.createUser(request);

        log.info("Usuário registrado com sucesso: {}", request.getEmail());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
    
}