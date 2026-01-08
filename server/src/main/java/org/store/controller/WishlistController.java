package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.WishlistDAO;
import org.store.model.Product;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:3000")
public class WishlistController {

    private final WishlistDAO wishlistDAO;

    @Autowired
    public WishlistController(WishlistDAO wishlistDAO) {
        this.wishlistDAO = wishlistDAO;
    }

    /**
     * GET: Retrieve wishlist.
     * URL: /api/wishlist?userId=123
     */
    @GetMapping
    public ResponseEntity<?> getWishlist(@RequestParam Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        List<Product> products = wishlistDAO.getWishlist(userId);
        return ResponseEntity.ok(products);
    }

    /**
     * POST: Add to wishlist.
     * JSON: { "userId": 1, "productId": 5 }
     */
    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            Long productId = ((Number) payload.get("productId")).longValue();

            if (wishlistDAO.addToWishlist(userId, productId)) {
                return ResponseEntity.ok(Map.of("message", "Added to wishlist"));
            } else {
                return ResponseEntity.ok(Map.of("message", "Already in wishlist or failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
        }
    }

    /**
     * DELETE: Remove from wishlist.
     * URL: /api/wishlist?userId=1&productId=5
     */
    @DeleteMapping
    public ResponseEntity<?> removeFromWishlist(@RequestParam Long userId, @RequestParam Long productId) {
        if (wishlistDAO.removeFromWishlist(userId, productId)) {
            return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        }
    }
}