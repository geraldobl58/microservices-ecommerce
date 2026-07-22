package com.ecomerce.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank(message = "The field is required!")
        String name,

        String description,

        @NotNull(message = "The field is required!")
        @Positive(message = "The price must be greater than zero!")
        BigDecimal price
) {
}
