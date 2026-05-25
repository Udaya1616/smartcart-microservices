package com.smartcart.orderservice.dto;

import java.math.BigDecimal;

public record OrderResponse(Long id, Long userId, Long productId, Integer quantity, BigDecimal totalAmount) {
}
