package com.pizzaria.service;

import com.pizzaria.entity.RefreshToken;
import com.pizzaria.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUserId(Long userId);

    RefreshToken findByToken(String token);
}
