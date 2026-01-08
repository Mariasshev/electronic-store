package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.model.User;
import org.store.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.Optional;

/**
 * Spring Boot REST Controller for managing user profiles.
 * Handles requests to '/api/user'.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET: Retrieve user profile details by email.
     * URL: /api/user?email=test@example.com
     */
    @GetMapping
    public ResponseEntity<?> getUser(@RequestParam String email) {
        // запит: SELECT * FROM users WHERE email = ?
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
    }

    /**
     * PUT: Update user profile OR change password.
     * <p>
     * Logic:
     * 1. If JSON contains "newPassword" -> Change Password mode.
     * 2. Otherwise -> Update Profile mode.
     * </p>
     * * We use Map<String, Object> to flexibly read the JSON body
     * because the structure changes depending on the operation.
     */
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // 1. знаходимо користувача в базі
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        User user = userOpt.get();

        // --- ЛОГІКА ЗМІНИ ПАРОЛЯ ---
        if (payload.containsKey("newPassword") && payload.containsKey("currentPassword")) {
            String currentPass = (String) payload.get("currentPassword");
            String newPass = (String) payload.get("newPassword");

            // 1. Перевірка:
            if (!BCrypt.checkpw(currentPass, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Incorrect current password"));
            }

            // 2. Хешуємо НОВИЙ пароль
            String hashedNewPass = BCrypt.hashpw(newPass, BCrypt.gensalt());

            user.setPassword(hashedNewPass);

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        }

        // --- ЛОГІКА ОНОВЛЕННЯ ПРОФІЛЮ ---
        try {
            if (payload.containsKey("username")) user.setUsername((String) payload.get("username"));
            if (payload.containsKey("phone")) user.setPhone((String) payload.get("phone"));
            if (payload.containsKey("address")) user.setAddress((String) payload.get("address"));

            userRepository.save(user);

            user.setPassword(null);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Update failed"));
        }
    }
}