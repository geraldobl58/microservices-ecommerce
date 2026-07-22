package com.ecomerce.order_service.controller;

import com.ecomerce.order_service.dto.OrderRequest;
import com.ecomerce.order_service.dto.OrderResponse;
import com.ecomerce.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        orderService.delete(id);
    }

}
