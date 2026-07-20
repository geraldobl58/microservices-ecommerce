package com.ecommerce.stock_service.controller;

import com.ecommerce.stock_service.dto.StockRequest;
import com.ecommerce.stock_service.dto.StockResponse;
import com.ecommerce.stock_service.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
class StockController {

    private final StockService stockService;

    @GetMapping("/{sku}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku") String sku, @RequestParam("quantity") Integer quantity) {
        return stockService.isInStock(sku, quantity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStock(@Valid @RequestBody StockRequest stockRequest) {
        return stockService.createStock(stockRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<StockResponse> getStocks() {
        return stockService.getAllStocks();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StockResponse updateStock(@PathVariable UUID id, @Valid @RequestBody StockRequest stockRequest) {
        return stockService.updateStock(id, stockRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
    }
}
