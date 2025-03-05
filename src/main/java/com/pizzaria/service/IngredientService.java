package com.pizzaria.service;

import com.pizzaria.dto.request.IngredientRequest;
import com.pizzaria.entity.Ingredient;

import java.math.BigDecimal;
import java.util.List;

public interface IngredientService {

    Ingredient createIngredient(IngredientRequest request);

    List<Ingredient> getAllIngredients();

    Ingredient getIngredientById(Long id);

    Ingredient updateIngredient(Long id, IngredientRequest request);

    List<Ingredient> getAvailableIngredients();

    List<Ingredient> searchIngredients(String name, BigDecimal maxPrice);

}
