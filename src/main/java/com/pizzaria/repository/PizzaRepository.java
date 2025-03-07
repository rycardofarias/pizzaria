package com.pizzaria.repository;

import com.pizzaria.entity.Pizza;
import com.pizzaria.enums.PizzaSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    @Query("SELECT p FROM Pizza p LEFT JOIN FETCH p.ingredients WHERE p.id = :id")
    Optional<Pizza> findByIdWithIngredients(@Param("id") Long id);

    @Query("SELECT p FROM Pizza p LEFT JOIN FETCH p.ingredients")
    List<Pizza> findAllWithIngredients();

    List<Pizza> findByNameContainingIgnoreCase(String name);

    List<Pizza> findBySize(PizzaSize size);

    List<Pizza> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
