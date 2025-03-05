package com.pizzaria.repository;

import com.pizzaria.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByAvailableTrue();

    List<Ingredient> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM Ingredient i WHERE i.price <= :maxPrice AND i.available = true")
    List<Ingredient> findByPriceLessThanEqual(BigDecimal maxPrice);

    boolean existsByNameIgnoreCase(String name);
}