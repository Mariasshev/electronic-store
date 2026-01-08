package org.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity class representing a Product in the store.
 * Simplified using Lombok.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Double oldPrice;

    private int stockQuantity;

    private String imageUrl;

    private Long categoryId;

    private Long brandId;

    // --- Additional fields for frontend display ---
    private String categoryName;
    private String brandName;

    private List<String> gallery = new ArrayList<>();

    private Map<String, String> specifications = new HashMap<>();

    private List<ProductColor> colors = new ArrayList<>();

    private List<ProductMemory> memoryOptions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductColor {
        private String name;
        private String hex;
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductMemory {
        private String size;
        private BigDecimal priceModifier;
        private int quantity;
    }
}