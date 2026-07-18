package com.eccomerce.product_service.service.impl;

import com.eccomerce.product_service.dto.ProductRequestDTO;
import com.eccomerce.product_service.dto.ProductResponseDTO;
import com.eccomerce.product_service.exception.ResourceNotFoundException;
import com.eccomerce.product_service.mapper.ProductMapper;
import com.eccomerce.product_service.model.Product;
import com.eccomerce.product_service.repository.ProductRepository;
import com.eccomerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Product product = mapper.toProduct(requestDTO);

        Product saveProduct = repository.save(product);

        return mapper.toProductResponseDTO(saveProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
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

        return mapper.toProductResponseDTO(updateProduct);
    }

    @Override
    public void deleteProduct(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        repository.deleteById(id);
    }
}
