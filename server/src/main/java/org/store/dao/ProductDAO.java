package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM elstore_products";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getLong("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setImageUrl(rs.getString("image_url"));

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> findByCategoryId(Long categoryId) {
        List<Product> products = new ArrayList<>();
        // ВАЖНО: Мы делаем JOIN, чтобы сразу получить названия бренда и категории (для хлебных крошек)
        String sql = "SELECT p.*, c.name AS category_name, b.name AS brand_name " +
                "FROM elstore_products p " +
                "LEFT JOIN elstore_categories c ON p.category_id = c.id " +
                "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                "WHERE p.category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getLong("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setImageUrl(rs.getString("image_url"));
                product.setCategoryName(rs.getString("category_name"));
                product.setBrandName(rs.getString("brand_name"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product findById(Long id) {
        Product product = null;

        // 1. Основний товар
        String sqlProduct =
                "SELECT p.*, c.name AS category_name, b.name AS brand_name " +
                        "FROM elstore_products p " +
                        "LEFT JOIN elstore_categories c ON p.category_id = c.id " +
                        "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                        "WHERE p.id = ?";

        // 2. Картинки
        String sqlImages = "SELECT image_url FROM elstore_product_images WHERE product_id = ?";

        // 3. Характеристики
        String sqlSpecs = "SELECT spec_key, spec_value FROM elstore_product_specs WHERE product_id = ? ORDER BY sort_order";

        // 4. Кольори
        String sqlColors = "SELECT color_name, hex_value, quantity FROM elstore_product_colors WHERE product_id = ?";

        // 5. ПАМ'ЯТЬ (НОВИЙ ЗАПИТ)
        String sqlMemory = "SELECT memory_size, price_modifier, quantity FROM elstore_product_memory WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // --- Крок A: Товар ---
            try (PreparedStatement pstmt = conn.prepareStatement(sqlProduct)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    product = new Product();
                    product.setId(rs.getLong("id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setStockQuantity(rs.getInt("stock_quantity"));
                    product.setImageUrl(rs.getString("image_url"));

                    // Breadcrumbs data
                    product.setCategoryName(rs.getString("category_name"));
                    product.setCategoryId(rs.getLong("category_id"));
                    product.setBrandName(rs.getString("brand_name"));
                }
            }

            if (product == null) return null;

            // --- Крок B: Галерея ---
            try (PreparedStatement pstmt = conn.prepareStatement(sqlImages)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                List<String> gallery = new ArrayList<>();
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    gallery.add(product.getImageUrl());
                }
                while (rs.next()) {
                    gallery.add(rs.getString("image_url"));
                }
                product.setGallery(gallery);
            }

            // --- Крок C: Характеристики ---
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSpecs)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                Map<String, String> specs = new java.util.LinkedHashMap<>();
                while (rs.next()) {
                    specs.put(rs.getString("spec_key"), rs.getString("spec_value"));
                }
                product.setSpecifications(specs);
            }

            // --- Крок D: Кольори ---
            try (PreparedStatement pstmt = conn.prepareStatement(sqlColors)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                List<Product.ProductColor> colorList = new ArrayList<>();
                while (rs.next()) {
                    colorList.add(new Product.ProductColor(
                            rs.getString("color_name"),
                            rs.getString("hex_value"),
                            rs.getInt("quantity")
                    ));
                }
                product.setColors(colorList);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlMemory)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                List<Product.ProductMemory> memoryList = new ArrayList<>();
                while (rs.next()) {
                    memoryList.add(new Product.ProductMemory(
                            rs.getString("memory_size"),
                            rs.getBigDecimal("price_modifier"),
                            rs.getInt("quantity")
                    ));
                }
                product.setMemoryOptions(memoryList);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }
}