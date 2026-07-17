package com.eccomerce.product_service.service;

import com.eccomerce.product_service.dto.ProductRequestDTO;
import com.eccomerce.product_service.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO getProductById(String id);
    ProductResponseDTO updateProduct(String id, ProductRequestDTO requestDTO);
    void deleteProduct(String id);
}
