package com.pizzaria.service;

import com.pizzaria.entity.User;

public interface SecurityService {
    boolean isOwner(Long userId);
    User getCurrentUser();
}
