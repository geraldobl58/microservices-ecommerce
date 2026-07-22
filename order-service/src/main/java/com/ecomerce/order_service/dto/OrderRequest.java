package com.ecomerce.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Order items list cannot be empty")
    @Valid
    private List<OrderItemsRequest> orderItems;
}
