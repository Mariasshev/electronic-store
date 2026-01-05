package org.store.model;

public class Address {
    private Long id;
    private Long userId;
    private String label;
    private String addressLine;
    private String phone;

    public Address() {}

    public Address(Long userId, String label, String addressLine, String phone) {
        this.userId = userId;
        this.label = label;
        this.addressLine = addressLine;
        this.phone = phone;
    }

    // Геттери і Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}