package com.pizzaria.service.impl;

import com.pizzaria.interfaces.Auditable;
import com.pizzaria.dto.request.IngredientRequest;
import com.pizzaria.entity.Ingredient;
import com.pizzaria.exception.BadRequestException;
import com.pizzaria.exception.ResourceNotFoundException;
import com.pizzaria.repository.IngredientRepository;
import com.pizzaria.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional
    @CachePut(value = "ingredient", key = "#result.id")
    @Auditable(action = "CREATE", entity = "Ingredient")
    public Ingredient createIngredient(IngredientRequest request) {
        log.info("Criando novo ingrediente: {}", request.getName());
        validateIngredientRequest(request);

        if (ingredientRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Tentativa de criar ingrediente com nome duplicado: {}", request.getName());
            throw new BadRequestException("Já existe um ingrediente com este nome");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setPrice(request.getPrice());
        ingredient.setAvailable(true);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        log.info("Ingrediente criado com sucesso. ID: {}", savedIngredient.getId());

        evictIngredientsCache();
        return savedIngredient;
    }

    @Cacheable(value = "ingredients")
    public List<Ingredient> getAllIngredients() {
        log.debug("Buscando todos os ingredientes - Cache MISS");
        return ingredientRepository.findAll();
    }

    @Cacheable(value = "ingredient", key = "#id")
    public Ingredient getIngredientById(Long id) {
        log.debug("Buscando ingrediente por ID: {} - Cache MISS", id);
        return ingredientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Ingrediente não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Ingrediente não encontrado");
                });
    }

    @Transactional
    @CachePut(value = "ingredient", key = "#id")
    public Ingredient updateIngredient(Long id, IngredientRequest request) {
        log.info("Atualizando ingrediente. ID: {}", id);
        validateIngredientRequest(request);

        Ingredient ingredient = getIngredientById(id);
        ingredient.setName(request.getName());
        ingredient.setPrice(request.getPrice());

        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        log.info("Ingrediente atualizado com sucesso. ID: {}", id);

        evictIngredientsCache();
        return updatedIngredient;
    }

    @Cacheable(value = "availableIngredients")
    public List<Ingredient> getAvailableIngredients() {
        log.debug("Buscando ingredientes disponíveis - Cache MISS");
        return ingredientRepository.findByAvailableTrue();
    }

    @Cacheable(value = "ingredients", key = "'search-' + #name + '-' + #maxPrice")
    public List<Ingredient> searchIngredients(String name, BigDecimal maxPrice) {
        log.debug("Buscando ingredientes com filtros - Cache MISS");
        if (name != null && !name.isEmpty()) {
            return ingredientRepository.findByNameContainingIgnoreCase(name);
        }

        if (maxPrice != null) {
            validatePrice(maxPrice);
            return ingredientRepository.findByPriceLessThanEqual(maxPrice);
        }

        return ingredientRepository.findByAvailableTrue();
    }

    private void validateIngredientRequest(IngredientRequest request) {
        log.debug("Validando request de ingrediente: {}", request);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            log.error("Nome do ingrediente é obrigatório");
            throw new BadRequestException("Nome do ingrediente é obrigatório");
        }
        if (request.getName().length() > 50) {
            log.error("Nome do ingrediente muito longo: {}", request.getName());
            throw new BadRequestException("Nome do ingrediente não pode ter mais que 50 caracteres");
        }
        validatePrice(request.getPrice());
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            log.error("Preço é obrigatório");
            throw new BadRequestException("Preço é obrigatório");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Preço inválido: {}", price);
            throw new BadRequestException("Preço deve ser maior que zero");
        }
        if (price.compareTo(new BigDecimal("100")) > 0) {
            log.error("Preço muito alto: {}", price);
            throw new BadRequestException("Preço não pode ser maior que R$ 100,00");
        }
    }

    @CacheEvict(value = {"ingredients", "availableIngredients"}, allEntries = true)
    protected void evictIngredientsCache() {
        log.debug("Limpando cache de ingredientes");
    }
}
