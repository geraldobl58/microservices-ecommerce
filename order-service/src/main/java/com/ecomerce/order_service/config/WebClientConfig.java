package com.ecomerce.order_service.config;

import com.ecomerce.order_service.service.client.StockClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClientBuilder() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082") // BASE URL for the stock service
                .build();
    }

    @Bean
    public StockClient stockClient(WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();

        return factory.createClient(StockClient.class);
    }
}
