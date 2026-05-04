package com.esi.week2.products.dto;

import com.esi.week2.products.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // Convert Product entity to ProductResponseDTO
    public ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    // Convert ProductRequestDTO to Product entity
    public Product toEntity(ProductRequestDTO dto) {
        return new Product(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice()
        );
    }

    // Update existing Product entity from ProductRequestDTO
    public void updateEntityFromDTO(ProductRequestDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
    }
}
