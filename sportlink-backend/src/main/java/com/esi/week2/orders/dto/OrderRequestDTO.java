package com.esi.week2.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String status;
}
