package org.store.model;

public class UserCard {
    private Long id;
    private Long userId;
    private String cardHolder;
    private String brand;
    private String last4;
    private String expiryDate;

    public UserCard() {}

    public UserCard(Long userId, String cardHolder, String brand, String last4, String expiryDate) {
        this.userId = userId;
        this.cardHolder = cardHolder;
        this.brand = brand;
        this.last4 = last4;
        this.expiryDate = expiryDate;
    }

    // Геттери та Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}