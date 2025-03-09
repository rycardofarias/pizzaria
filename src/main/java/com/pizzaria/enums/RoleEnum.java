package com.pizzaria.enums;

public enum RoleEnum {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_EMPLOYEE,
    ROLE_STOCK_MANAGER,
    ROLE_KITCHEN_MANAGER;

    public String getValue() {
        return this.name();
    }
} 