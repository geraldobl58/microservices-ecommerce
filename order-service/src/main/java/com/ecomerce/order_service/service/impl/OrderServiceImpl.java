package com.ecomerce.order_service.service.impl;

import com.ecomerce.order_service.dto.OrderRequest;
import com.ecomerce.order_service.dto.OrderResponse;
import com.ecomerce.order_service.exception.ResourceNotFoundException;
import com.ecomerce.order_service.mapper.OrderMapper;
import com.ecomerce.order_service.model.Order;
import com.ecomerce.order_service.repository.OrderRepository;
import com.ecomerce.order_service.service.OrderService;
import com.ecomerce.order_service.service.client.StockClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
//    private final WebClient.Builder webClientBuilder;
    private final StockClient stockClient;

//    @Value("${stock.service.url:http://localhost:8082}")
//    private String stockServiceUrl;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for customer");

        Order order = orderMapper.toOrder(orderRequest);

        for(var item : order.getOrderItems()) {
            String sku = item.getSku();
            Integer quantity = item.getQuantity();

            try {
//                String stockUrl = stockServiceUrl + "/api/v1/stock/reduce/" + sku;
//                log.debug("Calling stock service to reduce stock: {} with quantity: {}", stockUrl, quantity);
//
//                String response = webClientBuilder.build().put()
//                        .uri(stockUrl, uriBuilder -> uriBuilder
//                                .queryParam("quantity", quantity).build())
//                        .retrieve()
//                        .bodyToMono(String.class)
//                        .block();
//
//                log.info("Stock reduced successfully for SKU: {} - Response: {}", sku, response);

                stockClient.reduceStock(sku, quantity);

            } catch (WebClientResponseException e) {
                log.error("Stock service returned error for SKU {}: HTTP {} - {}", sku, e.getStatusCode(), e.getResponseBodyAsString());
                throw new IllegalArgumentException("Stock service error for SKU " + sku + ": " + e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                log.error("Error while calling stock service for SKU {}: {}", sku, e.getMessage(), e);
                throw new IllegalStateException("Could not verify stock for SKU " + sku + ". Stock service unavailable or SKU not found.", e);
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
