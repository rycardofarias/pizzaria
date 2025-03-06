package com.pizzaria.controller;

import com.pizzaria.dto.request.PizzaRequest;
import com.pizzaria.dto.response.PizzaResponse;
import com.pizzaria.entity.Pizza;
import com.pizzaria.service.PizzaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController {

    private final PizzaService pizzaService;

    @PostMapping
    public ResponseEntity<PizzaResponse> createPizza(@Valid @RequestBody PizzaRequest request) {
        log.info("Requisição para criar nova pizza: {}", request.getName());
        Pizza pizza = pizzaService.createPizza(request);
        log.debug("Pizza criada com sucesso. ID: {}", pizza.getId());
        return ResponseEntity.ok(PizzaResponse.fromEntity(pizza));
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<PizzaResponse>> getAllPizzas() {
        log.info("Requisição para listar todas as pizzas");
        List<Pizza> pizzas = pizzaService.getAllPizzas();
        List<PizzaResponse> response = pizzas.stream()
                .map(PizzaResponse::fromEntity)
                .collect(Collectors.toList());
        log.debug("Retornando {} pizzas", pizzas.size());
        return ResponseEntity.ok(response);
    }
}
