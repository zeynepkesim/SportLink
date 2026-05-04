package com.esi.week2.orders;

import com.esi.week2.products.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String status; // e.g., "PENDING", "COMPLETED", "CANCELLED"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    // Constructor without id (for creating new orders)
    public Order(Integer quantity, BigDecimal totalPrice, LocalDateTime orderDate, String status, Product product) {
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.product = product;
    }
}
