package com.pizzaria.service.impl;

import com.pizzaria.entity.RefreshToken;
import com.pizzaria.entity.User;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.TokenRefreshException;
import com.pizzaria.repository.RefreshTokenRepository;
import com.pizzaria.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(User user) {
        log.debug("Tentando criar refresh token para o usuário: {}", user.getEmail());

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        if (existingToken.isPresent()) {
            log.info("Refresh token já existe para o usuário: {}. Atualizando token.", user.getEmail());
            RefreshToken token = existingToken.get();
            token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            token.setToken(UUID.randomUUID().toString());  // Você pode decidir se deseja gerar um novo token ou manter o antigo
            refreshTokenRepository.save(token);  // Atualiza o token existente
            return token;
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Novo refresh token criado com sucesso para o usuário: {}", user.getEmail());
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.error("Refresh token expirado: {}", token.getToken());
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        log.info("Deletando refresh tokens do usuário ID: {}", userId);

        int deletedRows = refreshTokenRepository.deleteByUser(User.builder().id(userId).build());

        if (deletedRows == 0) {
            log.warn("Nenhum refresh token encontrado para o usuário ID: {}", userId);
            throw new BadRequestException("Nenhum refresh token encontrado para este usuário.");
        }
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Refresh token não encontrado: {}", token);
                    return new TokenRefreshException("Refresh token não encontrado");
                });
    }
}
