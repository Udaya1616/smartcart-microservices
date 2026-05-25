package com.smartcart.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull Long userId,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
