package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.UserCardDAO;
import org.store.model.UserCard;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:3000")
public class UserCardController {

    private final UserCardDAO cardDAO;

    @Autowired
    public UserCardController(UserCardDAO cardDAO) {
        this.cardDAO = cardDAO;
    }

    /**
     * GET: Retrieve saved cards for a user.
     * URL: /api/cards?userId=123
     */
    @GetMapping
    public List<UserCard> getUserCards(@RequestParam Long userId) {
        return cardDAO.getUserCards(userId);
    }

    /**
     * POST: Save a new card.
     * <p>
     * Logic:
     * 1. Takes the full card number from JSON.
     * 2. Extracts the last 4 digits.
     * 3. Determines the brand.
     * 4. Saves ONLY safe metadata to the database.
     * </p>
     */
    @PostMapping
    public ResponseEntity<?> addCard(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            String cardHolder = (String) payload.get("cardHolder");
            String expiryDate = (String) payload.get("expiryDate");
            String fullNumber = ((String) payload.get("cardNumber")).replaceAll("\\s+", "");

            // 1. Визначаємо Бренд
            String brand = "MASTERCARD";
            if (fullNumber.startsWith("4")) brand = "VISA";

            // 2. Беремо останні 4 цифри
            String last4 = fullNumber.length() >= 4 ? fullNumber.substring(fullNumber.length() - 4) : fullNumber;

            UserCard card = new UserCard(null, userId, cardHolder, brand, last4, expiryDate);

            if (cardDAO.addCard(card)) {
                return ResponseEntity.ok(Map.of("message", "Card added"));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to add card"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
        }
    }

    /**
     * DELETE: Remove a card.
     * URL: /api/cards?id=5
     */
    @DeleteMapping
    public ResponseEntity<?> deleteCard(@RequestParam Long id) {
        if (cardDAO.deleteCard(id)) {
            return ResponseEntity.ok(Map.of("message", "Card deleted"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete card"));
        }
    }
}