package com.esi.week2.products;

import com.esi.week2.products.dto.ProductMapper;
import com.esi.week2.products.dto.ProductRequestDTO;
import com.esi.week2.products.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    // Get all products
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get a single product by id
    public ProductResponseDTO getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toResponseDTO(product);
    }

    // Add a new product
    @Transactional
    public ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO) {
        Product product = productMapper.toEntity(productRequestDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(savedProduct);
    }

    // Update a product
    @Transactional
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO productRequestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productMapper.updateEntityFromDTO(productRequestDTO, product);
        product.setId(id); // Ensure id doesn't change
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(updatedProduct);
    }

    // Delete a product
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

/* OLD CODE - Before DTO refactoring
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get a single product by id
    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    // Add a new product
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    // Task 1: Update a product
    public void updateProduct(String id, Product product) {
        product.setId(id);
        productRepository.save(product);
    }

    // Task 4: Delete a product
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
*/
