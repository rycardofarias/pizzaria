package com.pizzaria.dto.response;

import com.pizzaria.entity.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    
    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }
} 