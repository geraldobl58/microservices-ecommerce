package com.ecomerce.order_service.service.impl;

import com.ecomerce.order_service.dto.OrderRequest;
import com.ecomerce.order_service.dto.OrderResponse;
import com.ecomerce.order_service.exception.ResourceNotFoundException;
import com.ecomerce.order_service.mapper.OrderMapper;
import com.ecomerce.order_service.model.Order;
import com.ecomerce.order_service.repository.OrderRepository;
import com.ecomerce.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WebClient.Builder webClientBuilder;


    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for customer");

        Order order = orderMapper.toOrder(orderRequest);

        for(var item : order.getOrderItems()) {
            String sku = item.getSku();
            Integer quantity = item.getQuantity();

            Boolean isInStock = webClientBuilder.build().get()
                    .uri("http://localhost:8081/api/v1/orders" + sku, uriBuilder -> uriBuilder
                            .queryParam("quantity", quantity).build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (!Boolean.TRUE.equals(isInStock)) {
                throw new IllegalArgumentException("Product with SKU " + sku + " is out of stock or insufficient quantity available.");

            }
        }

        order.setOrderNumber(UUID.randomUUID().toString());

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with order number: {}", savedOrder.getId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Order", "id", id)
                );
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);

        log.info("Order deleted successfully with order number: {}", id);
    }
}
