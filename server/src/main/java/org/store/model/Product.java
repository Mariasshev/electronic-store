package org.store.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl; // Головне фото
    private Long categoryId;
    private String categoryName;
    private String brandName;

    private List<String> gallery = new ArrayList<>();
    private Map<String, String> specifications = new HashMap<>();
    private List<ProductColor> colors = new ArrayList<>();

    // 1. Додаємо список для пам'яті
    private List<ProductMemory> memoryOptions = new ArrayList<>();

    public Product() {}

    public Product(String name, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // --- Геттери та Сеттери ---

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public List<ProductColor> getColors() { return colors; }
    public void setColors(List<ProductColor> colors) { this.colors = colors; }

    // 2. Геттер і Сеттер для пам'яті
    public List<ProductMemory> getMemoryOptions() { return memoryOptions; } // <--- НОВЕ
    public void setMemoryOptions(List<ProductMemory> memoryOptions) { this.memoryOptions = memoryOptions; } // <--- НОВЕ

    public List<String> getGallery() { return gallery; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public void addSpec(String key, String value) {
        this.specifications.put(key, value);
    }

    // --- Внутрішні класи ---

    public static class ProductColor {
        private String name;
        private String hex;
        private int quantity;

        public ProductColor(String name, String hex, int quantity) {
            this.name = name;
            this.hex = hex;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public String getHex() { return hex; }
        public int getQuantity() { return quantity; }
    }

    // 3. Клас для опису пам'яті
    public static class ProductMemory {
        private String size;
        private BigDecimal priceModifier;
        private int quantity;

        public ProductMemory(String size, BigDecimal priceModifier, int quantity) {
            this.size = size;
            this.priceModifier = priceModifier;
            this.quantity = quantity;
        }

        // ОБЯЗАТЕЛЬНО ДОБАВЬ ЭТИ СТРОКИ:
        public String getSize() { return size; }
        public BigDecimal getPriceModifier() { return priceModifier; }
        public int getQuantity() { return quantity; }
    }
}