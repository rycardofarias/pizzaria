package com.pizzaria.dto.request;

import com.pizzaria.enums.PizzaSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class PizzaRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String description;

    @NotNull(message = "Tamanho é obrigatório")
    private PizzaSize size;

    @NotNull(message = "Ingredientes são obrigatórios")
    @Size(min = 1, message = "Pizza deve ter pelo menos um ingrediente")
    private Set<Long> ingredientIds;
}
