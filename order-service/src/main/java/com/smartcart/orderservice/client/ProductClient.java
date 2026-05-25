package com.smartcart.orderservice.client;

import com.smartcart.orderservice.dto.ProductResponse;
import com.smartcart.orderservice.dto.StockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(@PathVariable Long id,
                               @RequestHeader("Authorization") String authorization);

    @PutMapping("/api/products/{id}/stock/reduce")
    ProductResponse reduceStock(@PathVariable Long id,
                                @RequestHeader("Authorization") String authorization,
                                @RequestBody StockRequest request);
}
