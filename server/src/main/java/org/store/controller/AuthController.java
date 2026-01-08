package org.store.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.model.User;
import org.store.repository.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Boot REST Controller for Authentication.
 * Handles:
 * - Registration (/api/auth/register)
 * - Login (/api/auth/login)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository userRepository;

    @Autowired
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * POST /register
     * Creates a new user account.
     * Expects JSON: { "username": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1. Перевірка
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        user.setRole("CLIENT");
        user.setCreatedAt(Timestamp.from(Instant.now()));

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    /**
     * POST /login
     * Authenticates existing user.
     * Expects JSON: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        // 1. Шукаємо юзера в базі
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 2. Перевіряємо пароль
            if (BCrypt.checkpw(password, user.getPassword())) {
                user.setPassword(null);
                return ResponseEntity.ok(user);
            }
        }

        // 4. Якщо email не знайдено/пароль не підійшов
        return ResponseEntity.status(401).body(Map.of("error", "Wrong email or password"));
    }
}