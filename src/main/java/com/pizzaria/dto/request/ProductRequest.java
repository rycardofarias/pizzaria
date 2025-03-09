package com.pizzaria.dto.request;

import com.pizzaria.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    private String description;
    
    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser maior que zero")
    private BigDecimal price;
    
    @NotNull(message = "Categoria é obrigatória")
    private ProductCategory category;
} 