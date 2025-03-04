package com.pizzaria.service;

import com.pizzaria.dto.request.IngredientRequest;
import com.pizzaria.entity.Ingredient;

import java.util.List;

public interface IngredientService {

    Ingredient createIngredient(IngredientRequest request);
    List<Ingredient> getAllIngredients();
}
