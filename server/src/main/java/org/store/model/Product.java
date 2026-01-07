package org.store.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity class representing a Product in the store.
 * Maps to the 'elstore_products' table in the database.
 */
public class Product {
    /** Unique identifier of the product */
    private Long id;

    /** Name of the product */
    private String name;

    /** Detailed description of the product */
    private String description;

    /** Current selling price */
    private BigDecimal price;

    /** * Previous price before discount.
     * Can be null if there is no discount.
     */
    private Double oldPrice;

    /** Available quantity in stock */
    private int stockQuantity;

    /** URL to the main product image */
    private String imageUrl;

    /** ID of the category this product belongs to */
    private Long categoryId;

    /** ID of the brand */
    private Long brandId;

    // Additional fields for frontend display
    private String categoryName;
    private String brandName;
    private List<String> gallery = new ArrayList<>();
    private Map<String, String> specifications = new HashMap<>();
    private List<ProductColor> colors = new ArrayList<>();
    private List<ProductMemory> memoryOptions = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Product() {}

    /**
     * Constructs a product with essential details.
     * @param name Product name
     * @param price Product price
     * @param stockQuantity Quantity in stock
     */
    public Product(String name, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // --- Getters and Setters with brief docs ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    /**
     * Gets the old price for discount calculation.
     * @return the old price or null if not applicable
     */
    public Double getOldPrice() { return oldPrice; }
    public void setOldPrice(Double oldPrice) { this.oldPrice = oldPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public List<String> getGallery() { return gallery; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }

    public List<ProductColor> getColors() { return colors; }
    public void setColors(List<ProductColor> colors) { this.colors = colors; }

    public List<ProductMemory> getMemoryOptions() { return memoryOptions; }
    public void setMemoryOptions(List<ProductMemory> memoryOptions) { this.memoryOptions = memoryOptions; }

    // --- Inner Classes ---

    /** Represents a color option for the product */
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

    /** Represents a memory configuration option */
    public static class ProductMemory {
        private String size;
        private BigDecimal priceModifier;
        private int quantity;

        public ProductMemory(String size, BigDecimal priceModifier, int quantity) {
            this.size = size;
            this.priceModifier = priceModifier;
            this.quantity = quantity;
        }
        public String getSize() { return size; }
        public BigDecimal getPriceModifier() { return priceModifier; }
        public int getQuantity() { return quantity; }
    }
}