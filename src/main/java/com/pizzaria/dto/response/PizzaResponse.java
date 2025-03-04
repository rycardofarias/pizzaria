package com.pizzaria.dto.response;

import com.pizzaria.entity.Pizza;
import com.pizzaria.enums.PizzaSize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PizzaResponse {
    private Long id;
    private String name;
    private String description;
    private PizzaSize size;
    private BigDecimal price;
    private boolean active;
    private Set<IngredientResponse> ingredients;

    public static PizzaResponse fromEntity(Pizza pizza) {
        PizzaResponse response = new PizzaResponse();
        response.setId(pizza.getId());
        response.setName(pizza.getName());
        response.setDescription(pizza.getDescription());
        response.setPrice(pizza.getPrice());
        response.setSize(pizza.getSize());
        response.setActive(pizza.isActive());
        response.setIngredients(pizza.getIngredients().stream()
                .map(IngredientResponse::fromEntity)
                .collect(Collectors.toSet()));
        return response;
    }
}