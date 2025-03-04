package com.pizzaria.enums;

import java.math.BigDecimal;

public enum PizzaSize {
    SMALL(new BigDecimal("35.00")),
    MEDIUM(new BigDecimal("45.00")),
    LARGE(new BigDecimal("55.00")),
    EXTRA_LARGE(new BigDecimal("65.00"));

    private final BigDecimal basePrice;

    PizzaSize(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}
