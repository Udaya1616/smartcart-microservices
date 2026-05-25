package com.smartcart.productservice.service;

import com.smartcart.productservice.dto.ProductRequest;
import com.smartcart.productservice.dto.ProductResponse;
import com.smartcart.productservice.dto.StockRequest;
import com.smartcart.productservice.entity.Product;
import com.smartcart.productservice.exception.BadRequestException;
import com.smartcart.productservice.exception.ResourceNotFoundException;
import com.smartcart.productservice.repository.ProductRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public List<ProductResponse> getProducts(String keyword, String sortBy) {
        Comparator<ProductResponse> comparator = switch (sortBy == null ? "id" : sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(ProductResponse::name, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(ProductResponse::price);
            case "quantity" -> Comparator.comparing(ProductResponse::quantity);
            default -> Comparator.comparing(ProductResponse::id);
        };

        return productRepository.findAll().stream()
                .map(this::toResponse)
                .filter(product -> keyword == null || keyword.isBlank()
                        || product.name().toLowerCase().contains(keyword.toLowerCase()))
                .sorted(comparator)
                .toList();
    }

    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public ProductResponse addProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        Product savedProduct = productRepository.save(product);
        log.info("Added product {}", savedProduct.getId());
        return toResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        applyRequest(product, request);
        Product savedProduct = productRepository.save(product);
        log.info("Updated product {}", savedProduct.getId());
        return toResponse(savedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product {}", id);
    }

    @Transactional
    public ProductResponse reduceStock(Long id, StockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        switch (request.operationOrDefault()) {
            case INCREASE -> product.setQuantity(product.getQuantity() + request.quantity());
            case REDUCE -> {
                if (product.getQuantity() < request.quantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + id);
                }
                product.setQuantity(product.getQuantity() - request.quantity());
            }
        }
        return toResponse(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getQuantity());
    }
}
