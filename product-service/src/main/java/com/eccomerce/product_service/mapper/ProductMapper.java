package com.eccomerce.product_service.mapper;

import com.eccomerce.product_service.dto.ProductRequestDTO;
import com.eccomerce.product_service.dto.ProductResponseDTO;
import com.eccomerce.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequestDTO requestDTO);

    ProductResponseDTO toProductResponseDTO(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequestDTO requestDTO, @MappingTarget Product product);
}
