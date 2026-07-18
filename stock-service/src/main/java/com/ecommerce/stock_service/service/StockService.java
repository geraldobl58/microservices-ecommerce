package com.ecommerce.stock_service.service;

import com.ecommerce.stock_service.dto.StockRequest;
import com.ecommerce.stock_service.dto.StockResponse;

import java.util.List;
import java.util.UUID;

public interface StockService {

    boolean isInStock(String sku, Integer quantity);
    List<StockResponse> getAllStocks();
    StockResponse createStock(StockRequest stockRequest);
    StockResponse updateStock(UUID id, StockRequest stockRequest);
    void deleteStock(UUID id);
}
