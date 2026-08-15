package com.ecommerce.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product-service", r -> r
                        .path("/api/v1/product", "/api/v1/product/**")
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order-service", r -> r
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .uri("lb://ORDER-SERVICE"))
                .route("stock-service", r -> r
                        .path("/api/v1/stock", "/api/v1/stock/**")
                        .uri("lb://STOCK-SERVICE"))
                .build();
    }
}
