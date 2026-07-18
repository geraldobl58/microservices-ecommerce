package com.ecommerce.stock_service.mapper;

import com.ecommerce.stock_service.dto.StockRequest;
import com.ecommerce.stock_service.dto.StockResponse;
import com.ecommerce.stock_service.model.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    Stock toModel(StockRequest stockRequest);

    @Mapping(target = "inStock", expression = "java(stock.getQuantity() > 0)")
    StockResponse toResponse(Stock stock);
}
