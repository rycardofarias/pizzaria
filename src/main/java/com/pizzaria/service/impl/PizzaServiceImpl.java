package com.pizzaria.service.impl;

import com.pizzaria.dto.request.PizzaRequest;
import com.pizzaria.entity.Ingredient;
import com.pizzaria.entity.Pizza;
import com.pizzaria.enums.PizzaSize;
import com.pizzaria.enums.ProductCategory;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.ResourceNotFoundException;
import com.pizzaria.repository.IngredientRepository;
import com.pizzaria.repository.PizzaRepository;
import com.pizzaria.service.PizzaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PizzaServiceImpl implements PizzaService {

    private final PizzaRepository pizzaRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    @CachePut(value = "pizza", key = "#result.id")
    public Pizza createPizza(PizzaRequest request) {
        log.info("Criando nova pizza: {}", request.getName());
        validatePizzaRequest(request);

        Pizza pizza = new Pizza();
        pizza.setName(request.getName());
        pizza.setDescription(request.getDescription());
        pizza.setCategory(ProductCategory.PIZZA);
        pizza.setSize(request.getSize());

        Set<Ingredient> ingredients = getIngredientsFromIds(request.getIngredientIds());
        pizza.setIngredients(ingredients);

        BigDecimal price = calculatePizzaPrice(pizza);
        pizza.setPrice(price);

        Pizza savedPizza = pizzaRepository.save(pizza);
        log.info("Pizza criada com sucesso. ID: {}", savedPizza.getId());
        return savedPizza;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pizzas")
    public List<Pizza> getAllPizzas() {
        log.debug("Buscando todas as pizzas - Cache MISS");
        return pizzaRepository.findAllWithIngredients();
    }

    @Cacheable(value = "pizza", key = "#id")
    public Pizza getPizzaById(Long id) {
        log.debug("Buscando pizza por ID: {} - Cache MISS", id);
        return pizzaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Pizza não encontrada. ID: {}", id);
                    return new ResourceNotFoundException("Pizza não encontrada");
                });
    }

    @Transactional
    @CachePut(value = "pizza", key = "#id")
    public Pizza updatePizza(Long id, PizzaRequest request) {
        log.info("Atualizando pizza. ID: {}", id);
        Pizza pizza = getPizzaById(id);

        pizza.setName(request.getName());
        pizza.setDescription(request.getDescription());
        pizza.setSize(request.getSize());

        Set<Ingredient> ingredients = getIngredientsFromIds(request.getIngredientIds());
        pizza.setIngredients(ingredients);

        BigDecimal price = calculatePizzaPrice(pizza);
        pizza.setPrice(price);

        Pizza updatedPizza = pizzaRepository.save(pizza);
        log.info("Pizza atualizada com sucesso. ID: {}", id);
        return updatedPizza;
    }

    @Transactional
    @CacheEvict(value = {"pizza", "pizzas"}, allEntries = true)
    public void deletePizza(Long id) {
        log.info("Desativando pizza. ID: {}", id);
        Pizza pizza = getPizzaById(id);
        pizza.setActive(false);
        pizzaRepository.save(pizza);
        log.info("Pizza desativada com sucesso. ID: {}", id);
    }

    @Cacheable(value = "pizzas", key = "'search-' + #name + '-' + #size + '-' + #minPrice + '-' + #maxPrice")
    public List<Pizza> searchPizzas(String name, PizzaSize size, BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Buscando pizzas com filtros - Cache MISS");

        if (name != null && !name.isEmpty()) {
            return pizzaRepository.findByNameContainingIgnoreCase(name);
        }

        if (size != null) {
            return pizzaRepository.findBySize(size);
        }

        if (minPrice != null && maxPrice != null) {
            return pizzaRepository.findByPriceBetween(minPrice, maxPrice);
        }

        return pizzaRepository.findAll();
    }

    private void validatePizzaRequest(PizzaRequest request) {
        if (request.getSize() == null) {
            throw new BadRequestException("Tamanho da pizza é obrigatório");
        }
        if (request.getIngredientIds() == null || request.getIngredientIds().isEmpty()) {
            throw new BadRequestException("Pizza deve ter pelo menos um ingrediente");
        }
        if (request.getIngredientIds().size() > 8) {
            throw new BadRequestException("Pizza não pode ter mais que 8 ingredientes");
        }
    }

    private Set<Ingredient> getIngredientsFromIds(Set<Long> ingredientIds) {
        log.debug("Buscando ingredientes por IDs: {}", ingredientIds);
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            log.error("Lista de ingredientes vazia");
            throw new BadRequestException("Pizza deve ter pelo menos um ingrediente");
        }

        Set<Ingredient> ingredients = ingredientIds.stream()
                .map(id -> ingredientRepository.findById(id)
                        .orElseThrow(() -> {
                            log.error("Ingrediente não encontrado. ID: {}", id);
                            return new ResourceNotFoundException("Ingrediente não encontrado: " + id);
                        }))
                .collect(Collectors.toSet());

        ingredients.forEach(ingredient -> {
            if (!ingredient.isAvailable()) {
                log.error("Ingrediente indisponível: {}", ingredient.getName());
                throw new BadRequestException("Ingrediente não disponível: " + ingredient.getName());
            }
        });

        return ingredients;
    }

    private BigDecimal calculatePizzaPrice(Pizza pizza) {
        log.debug("Calculando preço da pizza: {}", pizza.getName());
        BigDecimal basePrice = pizza.getSize().getBasePrice();

        BigDecimal ingredientsPrice = pizza.getIngredients().stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(ingredientsPrice);
    }
}
