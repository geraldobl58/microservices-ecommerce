package com.ecommerce.stock_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockRequest {

    @NotBlank(message = "SKU cannot be blank")
    private String sku;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;
}
