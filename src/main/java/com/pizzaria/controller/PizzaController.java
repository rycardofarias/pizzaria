package com.pizzaria.controller;

import com.pizzaria.dto.request.PizzaRequest;
import com.pizzaria.dto.response.PizzaResponse;
import com.pizzaria.entity.Pizza;
import com.pizzaria.enums.PizzaSize;
import com.pizzaria.service.PizzaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController extends BaseController {

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

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<PizzaResponse> getPizza(@PathVariable Long id) {
        log.info("Requisição para buscar pizza. ID: {}", id);
        Pizza pizza = pizzaService.getPizzaById(id);
        log.debug("Pizza encontrada com sucesso. ID: {}", id);
        return ResponseEntity.ok(PizzaResponse.fromEntity(pizza));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PizzaResponse> updatePizza(
            @PathVariable Long id,
            @Valid @RequestBody PizzaRequest request) {
        log.info("Requisição para atualizar pizza. ID: {}", id);
        Pizza pizza = pizzaService.updatePizza(id, request);
        log.debug("Pizza atualizada com sucesso. ID: {}", id);
        return ResponseEntity.ok(PizzaResponse.fromEntity(pizza));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePizza(@PathVariable Long id) {
        log.info("Requisição para desativar pizza. ID: {}", id);
        pizzaService.deletePizza(id);
        log.debug("Pizza desativada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<List<PizzaResponse>> searchPizzas(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PizzaSize size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        log.info("Requisição para buscar pizzas com filtros. name: {}, size: {}, preço: {} - {}",
                name, size, minPrice, maxPrice);

        List<Pizza> pizzas = pizzaService.searchPizzas(name, size, minPrice, maxPrice);
        List<PizzaResponse> response = pizzas.stream()
                .map(PizzaResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
