package com.pizzaria.service.impl;

import com.pizzaria.dto.request.ProductRequest;
import com.pizzaria.entity.Product;
import com.pizzaria.exception.ResourceNotFoundException;
import com.pizzaria.repository.ProductRepository;
import com.pizzaria.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        log.info("Buscando todos os produtos");
        List<Product> products = productRepository.findAll();
        log.info("Retornando {} produtos", products.size());
        return products;
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        log.info("Criando novo produto");
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        Product savedProduct = productRepository.save(product);
        log.info("Produto criado com ID: {}", savedProduct.getId());
        return savedProduct;
    }

    public Product getProductById(Long id) {
        log.info("Buscando produto por ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        log.info("Retornando produto com ID: {}", product.getId());
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        log.info("Atualizando produto com ID: {}", id);
        Product product = getProductById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);
        log.info("Produto atualizado com ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deletando produto com ID: {}", id);
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Produto deletado com ID: {}", id);
    }
}