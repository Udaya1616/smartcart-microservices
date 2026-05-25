package com.smartcart.orderservice.service;

import com.smartcart.orderservice.client.ProductClient;
import com.smartcart.orderservice.dto.OrderRequest;
import com.smartcart.orderservice.dto.OrderResponse;
import com.smartcart.orderservice.dto.ProductResponse;
import com.smartcart.orderservice.dto.StockRequest;
import com.smartcart.orderservice.entity.Order;
import com.smartcart.orderservice.exception.BadRequestException;
import com.smartcart.orderservice.exception.ServiceUnavailableException;
import com.smartcart.orderservice.repository.OrderRepository;
import com.smartcart.orderservice.security.JwtRoleService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final JwtRoleService jwtRoleService;

    public OrderResponse placeOrder(Long authenticatedUserId, String authorization, OrderRequest request) {
        if (!authenticatedUserId.equals(request.userId())) {
            throw new BadRequestException("Users can place orders only for their own account");
        }

        ProductResponse product = getProductWithResilience(request.productId(), authorization);
        if (product.quantity() < request.quantity()) {
            throw new BadRequestException("Insufficient stock for product: " + request.productId());
        }
        reduceStockWithResilience(request.productId(), request.quantity(), jwtRoleService.createServiceAdminAuthorizationHeader());

        Order order = new Order();
        order.setUserId(authenticatedUserId);
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(product.price().multiply(BigDecimal.valueOf(request.quantity())));
        Order savedOrder = orderRepository.save(order);
        log.info("Created order {} for user {}", savedOrder.getId(), savedOrder.getUserId());
        return toResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(OrderResponse::id).reversed())
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(OrderResponse::id).reversed())
                .toList();
    }

    @Retry(name = "productService", fallbackMethod = "productFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public ProductResponse getProductWithResilience(Long productId, String authorization) {
        return productClient.getProduct(productId, authorization);
    }

    @Retry(name = "productService", fallbackMethod = "stockFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "stockFallback")
    public ProductResponse reduceStockWithResilience(Long productId, Integer quantity, String authorization) {
        return productClient.reduceStock(productId, authorization, new StockRequest(quantity));
    }

    private ProductResponse productFallback(Long productId, String authorization, Throwable ex) {
        log.warn("Product service lookup failed for product {}", productId, ex);
        throw new ServiceUnavailableException("Product service is unavailable. Try again later.");
    }

    private ProductResponse stockFallback(Long productId, Integer quantity, String authorization, Throwable ex) {
        log.warn("Product stock update failed for product {}", productId, ex);
        throw new ServiceUnavailableException("Unable to update product stock. Try again later.");
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(), order.getProductId(),
                order.getQuantity(), order.getTotalAmount());
    }
}
