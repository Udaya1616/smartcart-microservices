package com.smartcart.productservice.controller;

import com.smartcart.productservice.dto.ProductRequest;
import com.smartcart.productservice.dto.ProductResponse;
import com.smartcart.productservice.dto.StockRequest;
import com.smartcart.productservice.security.JwtRoleService;
import com.smartcart.productservice.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final JwtRoleService jwtRoleService;

    @GetMapping
    public List<ProductResponse> getProducts(@RequestHeader(name = "Authorization", required = false) String authorization,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sortBy) {
        jwtRoleService.requireUser(authorization);
        return productService.getProducts(keyword, sortBy);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@RequestHeader(name = "Authorization", required = false) String authorization,
                                      @PathVariable Long id) {
        jwtRoleService.requireAuthenticated(authorization);
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse addProduct(@RequestHeader(name = "Authorization", required = false) String authorization,
                                      @Valid @RequestBody ProductRequest request) {
        jwtRoleService.requireAdmin(authorization);
        return productService.addProduct(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@RequestHeader(name = "Authorization", required = false) String authorization,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ProductRequest request) {
        jwtRoleService.requireAdmin(authorization);
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@RequestHeader(name = "Authorization", required = false) String authorization,
                              @PathVariable Long id) {
        jwtRoleService.requireAdmin(authorization);
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}/stock/reduce")
    public ProductResponse reduceStock(@RequestHeader(name = "Authorization", required = false) String authorization,
                                       @PathVariable Long id,
                                       @Valid @RequestBody StockRequest request) {
        jwtRoleService.requireAdmin(authorization);
        return productService.reduceStock(id, request);
    }

}
