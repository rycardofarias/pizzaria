package com.pizzaria.entity;

import com.pizzaria.enums.PizzaSize;
import com.pizzaria.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "ingredients")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_pizzas")
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private PizzaSize size;

    private BigDecimal price;
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "tb_pizza_ingredients",
            joinColumns = @JoinColumn(name = "pizza_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pizza)) return false;
        Pizza pizza = (Pizza) o;
        return id != null && id.equals(pizza.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}