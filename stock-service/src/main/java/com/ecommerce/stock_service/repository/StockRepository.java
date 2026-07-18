package com.ecommerce.stock_service.repository;

import com.ecommerce.stock_service.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findBySku(String sku);
    boolean existsBySku(String sku);
}
