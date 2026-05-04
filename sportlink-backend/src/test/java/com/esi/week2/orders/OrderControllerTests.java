package com.esi.week2.orders;

import com.esi.week2.orders.dto.OrderRequestDTO;
import com.esi.week2.orders.dto.OrderResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderResponseDTO orderResponseDTO;
    private OrderRequestDTO orderRequestDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime orderDate = LocalDateTime.of(2026, 5, 4, 10, 30);

        orderResponseDTO = new OrderResponseDTO(
                1L,
                "01",
                "Heavy vehicle",
                5,
                BigDecimal.valueOf(6000),
                orderDate,
                "PENDING"
        );

        orderRequestDTO = new OrderRequestDTO(
                "01",
                5,
                BigDecimal.valueOf(6000),
                orderDate,
                "PENDING"
        );
    }

    @Test
    void testGetAllOrders() throws Exception {
        // Arrange
        List<OrderResponseDTO> orders = Arrays.asList(
                orderResponseDTO,
                new OrderResponseDTO(2L, "02", "Medium vehicle", 3, BigDecimal.valueOf(5400),
                        LocalDateTime.now(), "COMPLETED")
        );
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].productId", is("01")))
                .andExpect(jsonPath("$[0].quantity", is(5)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetOrderById() throws Exception {
        // Arrange
        when(orderService.getOrder(1L)).thenReturn(orderResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productId", is("01")))
                .andExpect(jsonPath("$.productName", is("Heavy vehicle")))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.totalPrice", is(6000)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(orderService, times(1)).getOrder(1L);
    }

    @Test
    void testGetOrdersByProduct() throws Exception {
        // Arrange
        List<OrderResponseDTO> orders = Arrays.asList(orderResponseDTO);
        when(orderService.getOrdersByProduct("01")).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/orders/product/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId", is("01")));

        verify(orderService, times(1)).getOrdersByProduct("01");
    }

    @Test
    void testCreateOrder() throws Exception {
        // Arrange
        when(orderService.addOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productId", is("01")))
                .andExpect(jsonPath("$.quantity", is(5)));

        verify(orderService, times(1)).addOrder(any(OrderRequestDTO.class));
    }

    @Test
    void testUpdateOrder() throws Exception {
        // Arrange
        OrderRequestDTO updateRequest = new OrderRequestDTO(
                "01",
                10,
                BigDecimal.valueOf(12000),
                LocalDateTime.now(),
                "COMPLETED"
        );

        OrderResponseDTO updatedResponse = new OrderResponseDTO(
                1L,
                "01",
                "Heavy vehicle",
                10,
                BigDecimal.valueOf(12000),
                LocalDateTime.now(),
                "COMPLETED"
        );

        when(orderService.updateOrder(eq(1L), any(OrderRequestDTO.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(10)))
                .andExpect(jsonPath("$.totalPrice", is(12000)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(orderService, times(1)).updateOrder(eq(1L), any(OrderRequestDTO.class));
    }

    @Test
    void testDeleteOrder() throws Exception {
        // Arrange
        doNothing().when(orderService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void testGetOrderNotFound() throws Exception {
        // Arrange
        when(orderService.getOrder(99L)).thenThrow(new RuntimeException("Order not found with id: 99"));

        // Act & Assert
        mockMvc.perform(get("/orders/99"))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).getOrder(99L);
    }

    @Test
    void testCreateOrderWithInvalidProduct() throws Exception {
        // Arrange
        OrderRequestDTO invalidRequest = new OrderRequestDTO(
                "999",
                5,
                BigDecimal.valueOf(6000),
                LocalDateTime.now(),
                "PENDING"
        );

        when(orderService.addOrder(any(OrderRequestDTO.class)))
                .thenThrow(new RuntimeException("Product not found with id: 999"));

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).addOrder(any(OrderRequestDTO.class));
    }
}
