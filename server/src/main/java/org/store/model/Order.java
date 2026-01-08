package org.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Model representing a customer Order.
 * Contains general order info and a list of items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private BigDecimal totalPrice;
    private String status;
    private Timestamp createdAt;

    private List<OrderItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productName;
        private int quantity;
        private BigDecimal price;
        private String imageUrl;
    }
}