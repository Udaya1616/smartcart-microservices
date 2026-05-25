package com.smartcart.productservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull @Min(1) Integer quantity,
        StockOperation operation
) {
    public StockOperation operationOrDefault() {
        return operation == null ? StockOperation.REDUCE : operation;
    }

    public enum StockOperation {
        INCREASE,
        REDUCE;

        @JsonCreator
        public static StockOperation from(String value) {
            return value == null ? null : StockOperation.valueOf(value.toUpperCase());
        }
    }
}
