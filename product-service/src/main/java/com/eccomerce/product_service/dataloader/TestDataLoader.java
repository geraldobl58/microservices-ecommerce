package com.eccomerce.product_service.dataloader;

import com.eccomerce.product_service.model.Product;
import com.eccomerce.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) throws Exception {


        Product product = Product.builder()
                .name("Iphone 14")
                .description("Iphone 14")
                .price(BigDecimal.valueOf(1200))
                .build();

        productRepository.save(product);

        System.out.println("Product saved: " + product.getId() + " - " + product.getName() + " - " + product.getPrice());
    }
}
