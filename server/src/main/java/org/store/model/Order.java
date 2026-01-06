package org.store.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class Order {
    private Long id;
    private BigDecimal totalPrice;
    private String status;
    private Timestamp createdAt;
    private List<OrderItem> items; // Список товарів

    // Конструктори, Геттери, Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    // Внутрішній клас для позицій
    public static class OrderItem {
        private String productName;
        private int quantity;
        private BigDecimal price;
        private String imageUrl;

        public OrderItem(String productName, int quantity, BigDecimal price, String imageUrl) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public String getProductName() {
            return productName;
        }
        public void setProductName(String productName) {
            this.productName = productName;
        }
        public int getQuantity() {
            return quantity;
        }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        public BigDecimal getPrice() {
            return price;
        }
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

    }
}