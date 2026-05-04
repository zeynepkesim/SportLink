package com.esi.week2.orders.dto;

import com.esi.week2.orders.Order;
import com.esi.week2.products.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    // Convert Order entity to OrderResponseDTO
    public OrderResponseDTO toResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getOrderDate(),
                order.getStatus()
        );
    }

    // Convert OrderRequestDTO to Order entity
    public Order toEntity(OrderRequestDTO dto, Product product) {
        return new Order(
                dto.getQuantity(),
                dto.getTotalPrice(),
                dto.getOrderDate(),
                dto.getStatus(),
                product
        );
    }

    // Update existing Order entity from OrderRequestDTO
    public void updateEntityFromDTO(OrderRequestDTO dto, Order order, Product product) {
        order.setQuantity(dto.getQuantity());
        order.setTotalPrice(dto.getTotalPrice());
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(dto.getStatus());
        order.setProduct(product);
    }
}
