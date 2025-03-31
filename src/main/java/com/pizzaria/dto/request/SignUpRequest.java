package com.pizzaria.dto.request;

import com.pizzaria.entity.User;
import lombok.Data;

@Data
public class SignUpRequest {
    private String name;
    private String email;
    private String password;

    public User toEntity() {
        User user = new User();
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPassword(this.password);
        return user;
    }
}