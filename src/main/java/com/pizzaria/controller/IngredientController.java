package com.pizzaria.controller;

import com.pizzaria.dto.request.IngredientRequest;
import com.pizzaria.dto.response.IngredientResponse;
import com.pizzaria.entity.Ingredient;
import com.pizzaria.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody IngredientRequest request) {
        log.info("Requisição para criar novo ingrediente: {}", request.getName());
        Ingredient ingredient = ingredientService.createIngredient(request);
        log.debug("Ingrediente criado com sucesso. ID: {}", ingredient.getId());
        return ResponseEntity.ok(IngredientResponse.fromEntity(ingredient));
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllIngredients() {
        log.info("Requisição para listar todos os ingredientes");
        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        List<IngredientResponse> response = ingredients.stream()
                .map(IngredientResponse::fromEntity)
                .collect(Collectors.toList());
        log.debug("Retornando {} ingredientes", ingredients.size());
        return ResponseEntity.ok(response);
    }
}