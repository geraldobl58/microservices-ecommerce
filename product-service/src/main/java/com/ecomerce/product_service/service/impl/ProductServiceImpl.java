package com.ecomerce.product_service.service.impl;

import com.ecomerce.product_service.dto.ProductRequestDTO;
import com.ecomerce.product_service.dto.ProductResponseDTO;
import com.ecomerce.product_service.exception.ResourceNotFoundException;
import com.ecomerce.product_service.mapper.ProductMapper;
import com.ecomerce.product_service.model.Product;
import com.ecomerce.product_service.repository.ProductRepository;
import com.ecomerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Product product = mapper.toProduct(requestDTO);

        Product saveProduct = repository.save(product);

        log.info("Product {} created", saveProduct.getName());

        return mapper.toProductResponseDTO(saveProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Get all products");

       return repository.findAll()
               .stream()
               .map(mapper::toProductResponseDTO)
               .toList();
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product", "id", id)
                );

        return mapper.toProductResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO productRequestDTO) {
        Product product = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", id)
        );

        mapper.updateProductFromRequest(productRequestDTO, product);

        Product updateProduct = repository.save(product);

        log.info("Product {} updated", updateProduct.getName());

        return mapper.toProductResponseDTO(updateProduct);
    }

    @Override
    public void deleteProduct(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        repository.deleteById(id);

        log.info("Product {} deleted", id);
    }
}
