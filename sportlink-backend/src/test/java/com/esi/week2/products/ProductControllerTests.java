package com.esi.week2.products;

import com.esi.week2.products.dto.ProductRequestDTO;
import com.esi.week2.products.dto.ProductResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponseDTO productResponseDTO;
    private ProductRequestDTO productRequestDTO;

    @BeforeEach
    void setUp() {
        productResponseDTO = new ProductResponseDTO(
                "01",
                "Heavy vehicle",
                "Can be used for heavy work",
                BigDecimal.valueOf(1200)
        );

        productRequestDTO = new ProductRequestDTO(
                "01",
                "Heavy vehicle",
                "Can be used for heavy work",
                BigDecimal.valueOf(1200)
        );
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Arrange
        List<ProductResponseDTO> products = Arrays.asList(
                productResponseDTO,
                new ProductResponseDTO("02", "Medium vehicle", "Medium work", BigDecimal.valueOf(1800))
        );
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("01")))
                .andExpect(jsonPath("$[0].name", is("Heavy vehicle")))
                .andExpect(jsonPath("$[1].id", is("02")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testGetProductById() throws Exception {
        // Arrange
        when(productService.getProduct("01")).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/products/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("01")))
                .andExpect(jsonPath("$.name", is("Heavy vehicle")))
                .andExpect(jsonPath("$.description", is("Can be used for heavy work")))
                .andExpect(jsonPath("$.price", is(1200)));

        verify(productService, times(1)).getProduct("01");
    }

    @Test
    void testCreateProduct() throws Exception {
        // Arrange
        when(productService.addProduct(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("01")))
                .andExpect(jsonPath("$.name", is("Heavy vehicle")));

        verify(productService, times(1)).addProduct(any(ProductRequestDTO.class));
    }

    @Test
    void testUpdateProduct() throws Exception {
        // Arrange
        ProductRequestDTO updateRequest = new ProductRequestDTO(
                "01",
                "Heavy vehicle - Updated",
                "Updated description for heavy work",
                BigDecimal.valueOf(1500)
        );

        ProductResponseDTO updatedResponse = new ProductResponseDTO(
                "01",
                "Heavy vehicle - Updated",
                "Updated description for heavy work",
                BigDecimal.valueOf(1500)
        );

        when(productService.updateProduct(eq("01"), any(ProductRequestDTO.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/products/01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("01")))
                .andExpect(jsonPath("$.name", is("Heavy vehicle - Updated")))
                .andExpect(jsonPath("$.description", is("Updated description for heavy work")))
                .andExpect(jsonPath("$.price", is(1500)));

        verify(productService, times(1)).updateProduct(eq("01"), any(ProductRequestDTO.class));
    }

    @Test
    void testDeleteProduct() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct("01");

        // Act & Assert
        mockMvc.perform(delete("/products/01"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct("01");
    }

    @Test
    void testGetProductNotFound() throws Exception {
        // Arrange
        when(productService.getProduct("99")).thenThrow(new RuntimeException("Product not found with id: 99"));

        // Act & Assert
        mockMvc.perform(get("/products/99"))
                .andExpect(status().is5xxServerError());

        verify(productService, times(1)).getProduct("99");
    }
}
