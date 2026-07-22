package com.ecomerce.product_service.controller;

import com.ecomerce.product_service.dto.ProductRequestDTO;
import com.ecomerce.product_service.dto.ProductResponseDTO;
import com.ecomerce.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@RequestBody @Valid ProductRequestDTO productRequestDTO) {
        return productService.createProduct(productRequestDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDTO> getAllProduct() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO updateProduct(
            @PathVariable String id,
            @Valid
            @RequestBody ProductRequestDTO productRequestDTO) {

        return productService.updateProduct(id, productRequestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/test-fail")
    public void testFail() {
        throw new RuntimeException("test fail");
    }
}
