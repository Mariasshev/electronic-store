package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.CartDAO;
import org.store.model.CartItem;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    private final CartDAO cartDAO;

    @Autowired
    public CartController(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    /**
     * GET: Get cart items.
     * URL: /api/cart?userId=1
     */
    @GetMapping
    public List<CartItem> getCart(@RequestParam Long userId) {
        return cartDAO.getCart(userId);
    }

    /**
     * POST: Add item to cart.
     * JSON: { "userId": 1, "productId": 5, "quantity": 1 }
     */
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            Long productId = ((Number) payload.get("productId")).longValue();

            // За замовчуванням кількість 1, якщо не передано
            int quantity = payload.containsKey("quantity") ? ((Number) payload.get("quantity")).intValue() : 1;

            cartDAO.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Added to cart"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error adding to cart"));
        }
    }

    /**
     * PUT: Update quantity.
     * JSON: { "userId": 1, "productId": 5, "quantity": 3 }
     */
    @PutMapping
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            Long productId = ((Number) payload.get("productId")).longValue();
            int quantity = ((Number) payload.get("quantity")).intValue();

            cartDAO.updateQuantity(userId, productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Quantity updated"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error updating quantity"));
        }
    }

    /**
     * DELETE: Remove item.
     * URL: /api/cart?userId=1&productId=5
     */
    @DeleteMapping
    public ResponseEntity<?> removeFromCart(@RequestParam Long userId, @RequestParam Long productId) {
        if (cartDAO.removeFromCart(userId, productId)) {
            return ResponseEntity.ok(Map.of("message", "Removed"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Remove failed"));
        }
    }
}