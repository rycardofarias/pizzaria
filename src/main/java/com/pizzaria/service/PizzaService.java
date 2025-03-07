package com.pizzaria.service;

import com.pizzaria.dto.request.PizzaRequest;
import com.pizzaria.entity.Pizza;
import com.pizzaria.enums.PizzaSize;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public interface PizzaService {

    Pizza createPizza(PizzaRequest request);
    
    List<Pizza> getAllPizzas();

    Pizza getPizzaById(Long id);

    Pizza updatePizza(Long id, @Valid PizzaRequest request);

    void deletePizza(Long id);

    List<Pizza> searchPizzas(String name, PizzaSize size, BigDecimal minPrice, BigDecimal maxPrice);
}
