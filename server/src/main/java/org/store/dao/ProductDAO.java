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


    public List<Product> findWithFilters(Long categoryId, List<String> brands, Map<String, List<String>> specsFilters) {
        List<Product> products = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.description, p.price, p.stock_quantity, p.image_url, p.category_id, p.brand_id, " +
                        "b.name as brand_name " +
                        "FROM elstore_products p " +
                        "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                        "WHERE p.category_id = ? ");

        // 2. Фільтр по Бренду
        if (brands != null && !brands.isEmpty()) {
            sql.append(" AND UPPER(b.name) IN (");
            for (int i = 0; i < brands.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
            }
            sql.append(") ");
        }

        // 3. Фільтр по Характеристикам
        if (specsFilters != null && !specsFilters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : specsFilters.entrySet()) {
                sql.append(" AND EXISTS (SELECT 1 FROM elstore_product_specs s WHERE s.product_id = p.id " +
                        "AND UPPER(s.spec_key) = UPPER(?) AND UPPER(s.spec_value) IN (");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    sql.append(i == 0 ? "?" : ", ?");
                }
                sql.append(")) ");
            }
        }

        // ДЕБАГ:
//        System.out.println("EXECUTING SQL: " + sql.toString());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            pstmt.setLong(index++, categoryId);

            // Сетимо бренди
            if (brands != null) {
                for (String brand : brands) {
                    pstmt.setString(index++, brand.toUpperCase().trim());
                }
            }

            // специфікації
            if (specsFilters != null) {
                for (Map.Entry<String, List<String>> entry : specsFilters.entrySet()) {
                    pstmt.setString(index++, entry.getKey().toUpperCase().trim());
                    for (String val : entry.getValue()) {
                        pstmt.setString(index++, val.toUpperCase().trim());
                    }
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setImageUrl(rs.getString("image_url"));
                p.setCategoryId(rs.getLong("category_id"));
                p.setBrandId(rs.getLong("brand_id"));

                // Якщо в моделі є поле brandName
                p.setBrandName(rs.getString("brand_name"));

                products.add(p);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in findWithFilters: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    public org.store.dto.FilterDTO getFiltersByCategory(Long categoryId) {
        List<String> brands = new ArrayList<>();
        Map<String, List<String>> specs = new java.util.HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 1.бренди, які є в цій категорії
            String sqlBrands = "SELECT DISTINCT b.name FROM elstore_brands b " +
                    "JOIN elstore_products p ON p.brand_id = b.id " +
                    "WHERE p.category_id = ? ORDER BY b.name";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlBrands)) {
                pstmt.setLong(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) brands.add(rs.getString(1));
            }

            // 2. характеристики та їх значення
            String sqlSpecs = "SELECT DISTINCT s.spec_key, s.spec_value FROM elstore_product_specs s " +
                    "JOIN elstore_products p ON s.product_id = p.id " +
                    "WHERE p.category_id = ? ORDER BY s.spec_key, s.spec_value";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSpecs)) {
                pstmt.setLong(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String key = rs.getString("spec_key");
                    String val = rs.getString("spec_value");

                    specs.putIfAbsent(key, new ArrayList<>());
                    specs.get(key).add(val);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new org.store.dto.FilterDTO(brands, specs);
    }

    public List<Product> searchByName(String query) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, price, image_url FROM elstore_products " +
                "WHERE LOWER(name) LIKE ? ORDER BY name " +
                "FETCH NEXT 5 ROWS ONLY";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setImageUrl(rs.getString("image_url"));
                products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> findByCategoryId(Long categoryId) {
        List<Product> products = new ArrayList<>();
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

    public boolean createProductFull(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Вставляємо сам продукт
            String sqlProduct = "INSERT INTO elstore_products (name, description, price, stock_quantity, category_id, brand_id, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlProduct, new String[]{"id"}); // Очікуємо отримати згенерований ID

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setLong(5, product.getCategoryId());
            pstmt.setLong(6, product.getBrandId());
            pstmt.setString(7, product.getImageUrl());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating product failed, no rows affected.");

            // Отримуємо ID нового товару
            long newProductId;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newProductId = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }

            // 2. кольори (якщо є)
            if (product.getColors() != null && !product.getColors().isEmpty()) {
                String sqlColor = "INSERT INTO elstore_product_colors (product_id, color_name, hex_value, quantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtColor = conn.prepareStatement(sqlColor)) {
                    for (Product.ProductColor c : product.getColors()) {
                        pstmtColor.setLong(1, newProductId);
                        pstmtColor.setString(2, c.getName());
                        pstmtColor.setString(3, c.getHex());
                        pstmtColor.setInt(4, c.getQuantity());
                        pstmtColor.addBatch();
                    }
                    pstmtColor.executeBatch();
                }
            }

            // 3. характеристики (Specs)
            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
                String sqlSpec = "INSERT INTO elstore_product_specs (product_id, spec_key, spec_value, sort_order) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtSpec = conn.prepareStatement(sqlSpec)) {
                    int sortOrder = 1;
                    for (Map.Entry<String, String> entry : product.getSpecifications().entrySet()) {
                        pstmtSpec.setLong(1, newProductId);
                        pstmtSpec.setString(2, entry.getKey());
                        pstmtSpec.setString(3, entry.getValue());
                        pstmtSpec.setInt(4, sortOrder++);
                        pstmtSpec.addBatch();
                    }
                    pstmtSpec.executeBatch();
                }
            }

            // 4. (Gallery)
            if (product.getGallery() != null && !product.getGallery().isEmpty()) {
                String sqlImg = "INSERT INTO elstore_product_images (product_id, image_url) VALUES (?, ?)";
                try (PreparedStatement pstmtImg = conn.prepareStatement(sqlImg)) {
                    for (String imgUrl : product.getGallery()) {
                        pstmtImg.setLong(1, newProductId);
                        pstmtImg.setString(2, imgUrl);
                        pstmtImg.addBatch();
                    }
                    pstmtImg.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try { if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception e){}
        }
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

        // 5. ПАМ'ЯТЬ
        String sqlMemory = "SELECT memory_size, price_modifier, quantity FROM elstore_product_memory WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // --- Товар ---
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

            // --- Галерея ---
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

            // --- Характеристики ---
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSpecs)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                Map<String, String> specs = new java.util.LinkedHashMap<>();
                while (rs.next()) {
                    specs.put(rs.getString("spec_key"), rs.getString("spec_value"));
                }
                product.setSpecifications(specs);
            }

            // --- Кольори ---
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