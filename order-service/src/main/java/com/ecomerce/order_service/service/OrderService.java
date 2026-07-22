package com.ecomerce.order_service.service;

import com.ecomerce.order_service.dto.OrderItemsRequest;
import com.ecomerce.order_service.dto.OrderRequest;
import com.ecomerce.order_service.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);
    List<OrderResponse> findAll();
    OrderResponse findById(UUID id);
    void delete(UUID id);
}
