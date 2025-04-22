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
import com.pizzaria.exception.LockedException;
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
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    private String getIpHeadersRaw(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String xfHeader = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");
        sb.append("X-Forwarded-For: ").append(xfHeader != null ? xfHeader : "").append("; ");
        sb.append("X-Real-IP: ").append(realIp != null ? realIp : "");
        return sb.toString();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            String[] ips = xfHeader.split(",");
            for (String ip : ips) {
                ip = ip.trim();
                if (!ip.isEmpty() && !ip.equalsIgnoreCase("unknown")) {
                    return ip;
                }
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && !realIp.equalsIgnoreCase("unknown")) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/login")
    @Timed
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest, @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        log.info("Tentativa de login para o usuário: {} | Thread: {} | Timestamp: {}", request.getEmail(), Thread.currentThread().getId(), System.currentTimeMillis());
        String clientIp = extractClientIp(servletRequest);
        String ipHeaders = getIpHeadersRaw(servletRequest);
        try {
            // Chama o controle de tentativas e alerta
            authService.validateLoginAttempt(request.getEmail(), clientIp, ipHeaders, deviceFingerprint);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (!userDetails.isEmailVerified()) {
                log.warn("Usuário {} tentou fazer login, mas o email não está verificado.", request.getEmail());
                throw new BadRequestException("login.error.email_not_verified");
            }

            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser()).getToken();

            authService.loginSuccess(request.getEmail(), clientIp, deviceFingerprint);
            log.info("Login realizado com sucesso para usuário: {}", request.getEmail());

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken));

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            log.warn("Credenciais inválidas para o usuário: {}", request.getEmail());
            return buildErrorResponse(401, "login.error.invalid", "/api/auth/login", servletRequest.getLocale());
        } catch (LockedException | org.springframework.security.authentication.LockedException e) {
            log.warn("Usuário/IP/Device bloqueado: {}", request.getEmail());
            return buildErrorResponse(423, "login.error.locked", "/api/auth/login", servletRequest.getLocale());
        }
        catch (BadRequestException e) {
            log.warn("Erro de requisição: {}", e.getMessage());
            return buildErrorResponse(400, e.getMessage(), "/api/auth/login", servletRequest.getLocale());
        }
        catch (IllegalArgumentException e) {
            log.warn("Argumento inválido: {}", e.getMessage());
            return buildErrorResponse(400, "login.error.bad_request", "/api/auth/login", servletRequest.getLocale());
        }
    }



    @PostMapping("/refresh")
    @Timed(value = "auth.refresh.endpoint", description = "Time taken to process token refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request, HttpServletRequest servletRequest) {
        log.info("Tentativa de refresh token");

        try {
            authService.validateRefreshTokenAttempt(request.getRefreshToken());

            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            String token = tokenProvider.generateTokenFromUser(user);

            authService.refreshTokenSuccess();
            String ipAddress = extractClientIp(servletRequest);
            authService.auditRefreshToken(user.getEmail(), ipAddress);
            log.info("Token renovado com sucesso para usuário: {}", user.getEmail());

            return ResponseEntity.ok(new TokenRefreshResponse(token, request.getRefreshToken()));
        } catch (TokenRefreshException e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            return buildErrorResponse(400, "refresh.error.invalid", "/api/auth/refresh", servletRequest.getLocale());
        } catch (IllegalArgumentException e) {
            log.warn("Argumento inválido ao renovar token: {}", e.getMessage());
            return buildErrorResponse(400, "login.error.bad_request", "/api/auth/refresh", servletRequest.getLocale());
        } catch (Exception e) {
            log.error("Erro inesperado ao renovar token: {}", e.getMessage());
            return buildErrorResponse(500, "login.error.unexpected", "/api/auth/refresh", servletRequest.getLocale());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token, HttpServletRequest servletRequest) {
        log.info("Tentativa de logout");
        try {
            String jwt = token.substring(7);
            String username = tokenProvider.getUsernameFromJWT(jwt);
            User user = userService.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

            refreshTokenService.deleteByUserId(user.getId());

            String ipAddress = extractClientIp(servletRequest);
            authService.auditLogout(user.getEmail(), ipAddress);

            log.info("Logout realizado com sucesso");
            return ResponseEntity.ok().build();
        } catch (UsernameNotFoundException e) {
            log.warn("Usuário não encontrado ao tentar logout: {}", e.getMessage());
            return buildErrorResponse(404, "login.error.user_not_found", "/api/auth/logout", servletRequest.getLocale());
        } catch (IllegalArgumentException e) {
            log.warn("Argumento inválido ao tentar logout: {}", e.getMessage());
            return buildErrorResponse(400, "login.error.bad_request", "/api/auth/logout", servletRequest.getLocale());
        } catch (Exception e) {
            log.error("Erro inesperado ao tentar logout: {}", e.getMessage());
            return buildErrorResponse(500, "login.error.unexpected", "/api/auth/logout", servletRequest.getLocale());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest request, HttpServletRequest servletRequest) {
        log.info("Tentativa de registro para usuário: {}", request.getEmail());
        try {
            User user = userService.createUser(request);
            String ipAddress = extractClientIp(servletRequest);
            authService.auditRegister(user.getEmail(), ipAddress);
            log.info("Usuário registrado com sucesso: {}", request.getEmail());
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (IllegalArgumentException e) {
            log.warn("Argumento inválido ao registrar usuário: {}", e.getMessage());
            return buildErrorResponse(400, "login.error.bad_request", "/api/auth/register", servletRequest.getLocale());
        } catch (BadRequestException e) {
            log.warn("Erro de requisição ao registrar usuário: {}", e.getMessage());
            return buildErrorResponse(400, e.getMessage(), "/api/auth/register", servletRequest.getLocale());
        } catch (Exception e) {
            log.error("Erro inesperado ao registrar usuário: {}", e.getMessage());
            return buildErrorResponse(500, "login.error.unexpected", "/api/auth/register", servletRequest.getLocale());
        }
    }
}