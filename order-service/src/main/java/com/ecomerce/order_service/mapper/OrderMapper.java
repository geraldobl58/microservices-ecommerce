package com.ecomerce.order_service.mapper;

import com.ecomerce.order_service.dto.OrderItemsResponse;
import com.ecomerce.order_service.dto.OrderRequest;
import com.ecomerce.order_service.dto.OrderResponse;
import com.ecomerce.order_service.model.Order;
import com.ecomerce.order_service.model.OrderItems;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toOrder(OrderRequest orderRequest);
    OrderItems toOrderItems(OrderRequest orderRequest);
    OrderResponse toOrderResponse(Order order);
    OrderItemsResponse toOrderItemsResponse(OrderItems orderItems);
}
