package com.pizzaria.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    private String name;
    
    @Email(message = "Email deve ser válido")
    private String email;
    
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;
} 