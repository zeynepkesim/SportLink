package com.esi.week2.orders;

import com.esi.week2.orders.dto.OrderMapper;
import com.esi.week2.orders.dto.OrderRequestDTO;
import com.esi.week2.orders.dto.OrderResponseDTO;
import com.esi.week2.products.Product;
import com.esi.week2.products.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderMapper orderMapper;

    // Get all orders
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get a single order by id
    public OrderResponseDTO getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return orderMapper.toResponseDTO(order);
    }

    // Get all orders for a specific product
    public List<OrderResponseDTO> getOrdersByProduct(String productId) {
        return orderRepository.findByProductId(productId).stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Add a new order
    @Transactional
    public OrderResponseDTO addOrder(OrderRequestDTO orderRequestDTO) {
        Product product = productRepository.findById(orderRequestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + orderRequestDTO.getProductId()));

        Order order = orderMapper.toEntity(orderRequestDTO, product);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponseDTO(savedOrder);
    }

    // Update an order
    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO orderRequestDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        Product product = productRepository.findById(orderRequestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + orderRequestDTO.getProductId()));

        orderMapper.updateEntityFromDTO(orderRequestDTO, order, product);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponseDTO(updatedOrder);
    }

    // Delete an order
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
