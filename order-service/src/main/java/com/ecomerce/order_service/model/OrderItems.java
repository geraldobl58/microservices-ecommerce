package com.ecomerce.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String sku;
    private BigDecimal price;
    private Integer quantity;
}
