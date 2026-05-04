package com.esi.week2.orders;

import com.esi.week2.orders.dto.OrderRequestDTO;
import com.esi.week2.orders.dto.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // GET all orders
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // GET a single order by id
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    // GET all orders for a specific product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByProduct(@PathVariable String productId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByProduct(productId);
        return ResponseEntity.ok(orders);
    }

    // POST - Create a new order
    @PostMapping
    public ResponseEntity<OrderResponseDTO> addOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO createdOrder = orderService.addOrder(orderRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    // PUT - Update an order
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO updatedOrder = orderService.updateOrder(id, orderRequestDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    // DELETE - Delete an order
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
