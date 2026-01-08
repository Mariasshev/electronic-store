package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.OrderDAO;
import org.store.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot REST Controller for managing orders.
 * Handles requests to '/api/orders'.
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    private final OrderDAO orderDAO;

    @Autowired
    public OrderController(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * GET: Retrieve order history for a specific user.
     * Usage: /api/orders?userId=123
     */
    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestParam Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }

        List<Order> orders = orderDAO.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * POST: Create a new order (Checkout).
     * Expects JSON: { "userId": 1, "address": "...", "phone": "...", "total": 100.50 }
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            String address = (String) payload.get("address");
            String phone = (String) payload.get("phone");
            BigDecimal total = new BigDecimal(payload.get("total").toString());

            orderDAO.createOrder(userId, address, phone, total);

            return ResponseEntity.ok(Map.of("message", "Order created successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Order creation failed: " + e.getMessage()));
        }
    }
}