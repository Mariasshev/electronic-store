package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.AddressDAO;
import org.store.model.Address;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:3000")
public class AddressController {

    private final AddressDAO addressDAO;

    @Autowired
    public AddressController(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    /**
     * GET: Retrieve addresses for a user.
     * URL: /api/addresses?userId=123
     */
    @GetMapping
    public List<Address> getAddresses(@RequestParam Long userId) {
        return addressDAO.findByUserId(userId);
    }

    /**
     * POST: Add a new address.
     */
    @PostMapping
    public ResponseEntity<?> addAddress(@RequestBody Address address) {
        if (addressDAO.addAddress(address)) {
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Address added"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add address"));
        }
    }

    /**
     * DELETE: Remove an address.
     * URL: /api/addresses?id=5
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAddress(@RequestParam Long id) {
        if (addressDAO.deleteAddress(id)) {
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Address deleted"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete address"));
        }
    }
}