package com.ecommerce.stock_service.service.impl;

import com.ecommerce.stock_service.dto.StockRequest;
import com.ecommerce.stock_service.dto.StockResponse;
import com.ecommerce.stock_service.mapper.StockMapper;
import com.ecommerce.stock_service.model.Stock;
import com.ecommerce.stock_service.repository.StockRepository;
import com.ecommerce.stock_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(String sku, Integer quantity) {
        return stockRepository.findBySku(sku)
                .map(stock -> stock.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public StockResponse createStock(StockRequest stockRequest) {
        boolean exists = stockRepository.existsBySku(stockRequest.getSku());

        if (exists) {
            throw new RuntimeException("Stock with SKU " + stockRequest.getSku() + " already exists.");
        }

        Stock stock = stockMapper.toModel(stockRequest);
        Stock savedStock = stockRepository.save(stock);

        log.info("Stock created with SKU: {}", savedStock.getSku());

        return stockMapper.toResponse(savedStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(stockMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public StockResponse updateStock(UUID id, StockRequest stockRequest) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Stock with ID " + id + " not found.")
                );

        stock.setSku(stockRequest.getSku());
        stock.setQuantity(stockRequest.getQuantity());

        Stock updatedStock = stockRepository.save(stock);

        log.info("Stock updated with ID: {}", updatedStock.getId());
        return stockMapper.toResponse(updatedStock);
    }

    @Override
    @Transactional
    public void deleteStock(UUID id) {
       if (!stockRepository.existsById(id)) {
           throw new RuntimeException("Stock with ID " + id + " not found.");
       }

       stockRepository.deleteById(id);

       log.info("Stock deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public void reduceStock(String sku, Integer quantity) {
        var stock = stockRepository.findBySku(sku)
                .orElseThrow(
                        () -> new RuntimeException("Stock with SKU " + sku + " not found.")
                );

        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for SKU " + sku);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }
}
