package com.pizzaria.dto.response;

import com.pizzaria.entity.Product;
import com.pizzaria.enums.ProductCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private boolean active;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setCategory(product.getCategory());
        response.setActive(product.isActive());
        return response;
    }
} 