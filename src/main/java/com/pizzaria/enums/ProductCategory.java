package com.pizzaria.enums;

public enum ProductCategory {
    PIZZA("Pizza"),
    DRINK("Bebida"),
    DESSERT("Sobremesa"),
    SIDE_DISH("Acompanhamento");

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 
