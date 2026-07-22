package com.ecomerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemsResponse {

    private UUID id;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
}
