package com.ecomerce.product_service.controller;

import com.ecomerce.product_service.dto.ProductRequestDTO;
import com.ecomerce.product_service.dto.ProductResponseDTO;
import com.ecomerce.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@RefreshScope
class ProductController {

    private final ProductService productService;

    @Value("${maintenance.message: System Operation!}")
    private String maintenanceMessage;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@RequestBody @Valid ProductRequestDTO productRequestDTO) {
        return productService.createProduct(productRequestDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDTO> getAllProduct(HttpServletResponse response) {
        response.addHeader("X-Maintenance-Message", maintenanceMessage);
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
