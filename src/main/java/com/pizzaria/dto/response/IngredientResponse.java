package com.pizzaria.dto.response;

import com.pizzaria.entity.Ingredient;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngredientResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean available;

    public static IngredientResponse fromEntity(Ingredient ingredient) {
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setPrice(ingredient.getPrice());
        response.setAvailable(ingredient.isAvailable());
        return response;
    }
} 