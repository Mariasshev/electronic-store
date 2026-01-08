package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.PromoCodeDAO;

import java.util.Map;

/**
 * Spring Boot REST Controller for validating promo codes.
 * Handles requests to '/api/promo'.
 */
@RestController
@RequestMapping("/api/promo")
@CrossOrigin(origins = "http://localhost:3000")
public class PromoCodeController {

    private final PromoCodeDAO promoCodeDAO;

    @Autowired
    public PromoCodeController(PromoCodeDAO promoCodeDAO) {
        this.promoCodeDAO = promoCodeDAO;
    }

    /**
     * POST: Validate a promo code.
     * <p>
     * Request:  {"code": "SUMMER2024"}
     * Response (Success): {"valid": true, "discountPercent": 10}
     * Response (Failure): {"valid": false}
     * </p>
     */
    @PostMapping
    public ResponseEntity<?> checkPromo(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");

        // Валідація вхідних даних
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Promo code is required"));
        }

        int discount = promoCodeDAO.getDiscount(code);

        if (discount > 0) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "discountPercent", discount
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "valid", false
            ));
        }
    }
}