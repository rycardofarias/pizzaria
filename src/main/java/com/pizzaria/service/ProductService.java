package com.pizzaria.service;

import com.pizzaria.dto.request.ProductRequest;
import com.pizzaria.entity.Product;
import jakarta.validation.Valid;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product createProduct(@Valid ProductRequest request);

    Product getProductById(Long id);

    Product updateProduct(Long id, @Valid ProductRequest request);

    void deleteProduct(Long id);
}
