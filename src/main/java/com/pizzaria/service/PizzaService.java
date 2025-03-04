package com.pizzaria.service;

import com.pizzaria.dto.request.PizzaRequest;
import com.pizzaria.entity.Pizza;

import java.util.List;

public interface PizzaService {

    Pizza createPizza(PizzaRequest request);
    List<Pizza> getAllPizzas();
}
