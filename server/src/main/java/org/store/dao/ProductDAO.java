package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.store.dto.FilterDTO;
import org.store.model.Product;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Data Access Object (DAO) for Product operations using Spring JDBC.
 * <p>
 * This class handles all low-level database interactions for Products, including:
 * <ul>
 * <li>Retrieving products with dynamic filters (Brand, Category, Specifications).</li>
 * <li>Creating new products with transactional support (saving images, colors, specs).</li>
 * <li>Fetching detailed product information.</li>
 * </ul>
 * </p>
 * Annotated with {@code @Repository} to be detected by Spring component scanning.
 */
@Repository
public class ProductDAO {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor for Dependency Injection.
     * Spring automatically injects the configured {@link JdbcTemplate}.
     *
     * @param jdbcTemplate The JDBC template for database operations.
     */
    @Autowired
    public ProductDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper to convert a ResultSet row into a {@link Product} object.
     * <p>
     * Maps basic fields (id, name, price) and joined fields (category_name, brand_name).
     * Used internally by query methods.
     * </p>
     */
    private final RowMapper<Product> productMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setOldPrice(rs.getObject("old_price", Double.class));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setBrandId(rs.getLong("brand_id"));

        // Optional fields from LEFT JOINs (might be null)
        try { p.setCategoryName(rs.getString("category_name")); } catch (Exception ignored) {}
        try { p.setBrandName(rs.getString("brand_name")); } catch (Exception ignored) {}

        return p;
    };

    /**
     * Retrieves all products from the database without filtering.
     *
     * @return List of all {@link Product} objects.
     */
    public List<Product> findAll() {
        String sql = "SELECT * FROM elstore_products";
        return jdbcTemplate.query(sql, productMapper);
    }

    /**
     * Retrieves all products belonging to a specific category.
     * Performs joins to include category and brand names.
     *
     * @param categoryId The unique identifier of the category.
     * @return List of products in the category.
     */
    public List<Product> findByCategoryId(Long categoryId) {
        String sql = "SELECT p.*, c.name AS category_name, b.name AS brand_name " +
                "FROM elstore_products p " +
                "LEFT JOIN elstore_categories c ON p.category_id = c.id " +
                "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                "WHERE p.category_id = ?";
        return jdbcTemplate.query(sql, productMapper, categoryId);
    }

    /**
     * Searches for products by name using a LIKE query.
     * Results are limited to 5 items (useful for autocomplete).
     *
     * @param query The search string (e.g., "iPhone").
     * @return List of matching products (max 5).
     */
    public List<Product> searchByName(String query) {
        String sql = "SELECT * FROM elstore_products WHERE LOWER(name) LIKE ? ORDER BY name FETCH NEXT 5 ROWS ONLY";
        String param = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(sql, productMapper, param);
    }

    /**
     * Universal search method with dynamic criteria.
     * Can filter by Category, Brand, and Search Query simultaneously.
     * Used for "Related Products", general Catalog view, and Search results.
     *
     * @param categoryId  The category ID (nullable).
     * @param brandId     The brand ID (nullable).
     * @param searchQuery The search text (nullable).
     * @return List of products matching the criteria.
     */
    public List<Product> findProducts(Long categoryId, Long brandId, String searchQuery) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as category_name, b.name as brand_name " +
                        "FROM elstore_products p " +
                        "LEFT JOIN elstore_categories c ON p.category_id = c.id " +
                        "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                        "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (brandId != null) {
            sql.append(" AND p.brand_id = ?");
            params.add(brandId);
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND LOWER(p.name) LIKE ?");
            params.add("%" + searchQuery.toLowerCase().trim() + "%");
        }

        sql.append(" ORDER BY p.id DESC");

        return jdbcTemplate.query(sql.toString(), productMapper, params.toArray());
    }

    /**
     * Advanced filtering method for the Catalog page.
     * Supports filtering by:
     * <ul>
     * <li>Category (required)</li>
     * <li>List of Brands (checkboxes)</li>
     * <li>Dynamic Specifications (e.g., "RAM" -> "16GB", "8GB")</li>
     * </ul>
     *
     * @param categoryId   The category ID.
     * @param brands       List of brand names to filter by.
     * @param specsFilters Map of specifications (Key -> List of Values).
     * @return List of filtered products.
     */
    public List<Product> findWithFilters(Long categoryId, List<String> brands, Map<String, List<String>> specsFilters) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as category_name, b.name as brand_name " +
                        "FROM elstore_products p " +
                        "LEFT JOIN elstore_categories c ON p.category_id = c.id " +
                        "LEFT JOIN elstore_brands b ON p.brand_id = b.id " +
                        "WHERE p.category_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(categoryId);

        // Filter by Brands
        if (brands != null && !brands.isEmpty()) {
            sql.append(" AND UPPER(b.name) IN (");
            for (int i = 0; i < brands.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
                params.add(brands.get(i).toUpperCase().trim());
            }
            sql.append(") ");
        }

        // Filter by Specifications (Dynamic Attributes)
        if (specsFilters != null && !specsFilters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : specsFilters.entrySet()) {
                sql.append(" AND EXISTS (SELECT 1 FROM elstore_product_specs s WHERE s.product_id = p.id " +
                        "AND UPPER(s.spec_key) = UPPER(?) AND UPPER(s.spec_value) IN (");
                params.add(entry.getKey().trim());

                List<String> values = entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    sql.append(i == 0 ? "?" : ", ?");
                    params.add(values.get(i).toUpperCase().trim());
                }
                sql.append(")) ");
            }
        }

        return jdbcTemplate.query(sql.toString(), productMapper, params.toArray());
    }

    /**
     * Retrieves available filter options (brands and specs) for a specific category.
     * Essential for populating the filter sidebar on the frontend.
     *
     * @param categoryId The category ID.
     * @return A {@link FilterDTO} containing available brands and specs.
     */
    public FilterDTO getFiltersByCategory(Long categoryId) {
        // 1. Fetch Distinct Brands
        String sqlBrands = "SELECT DISTINCT b.name FROM elstore_brands b " +
                "JOIN elstore_products p ON p.brand_id = b.id " +
                "WHERE p.category_id = ? ORDER BY b.name";
        List<String> brands = jdbcTemplate.queryForList(sqlBrands, String.class, categoryId);

        // 2. Fetch Distinct Specifications
        String sqlSpecs = "SELECT DISTINCT s.spec_key, s.spec_value FROM elstore_product_specs s " +
                "JOIN elstore_products p ON s.product_id = p.id " +
                "WHERE p.category_id = ? ORDER BY s.spec_key, s.spec_value";

        Map<String, List<String>> specs = new HashMap<>();
        jdbcTemplate.query(sqlSpecs, (rs) -> {
            String key = rs.getString("spec_key");
            String val = rs.getString("spec_value");
            specs.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
        }, categoryId);

        return new FilterDTO(brands, specs);
    }

    /**
     * Finds a single product by ID and loads ALL related details.
     * Includes: Gallery images, Specifications, Color options, Memory options.
     *
     * @param id The product ID.
     * @return The full {@link Product} object or null if not found.
     */
    public Product findById(Long id) {
        String sql = "SELECT * FROM elstore_products WHERE id = ?";

        try {

            Product product = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setCategoryId(rs.getLong("category_id"));
                p.setBrandId(rs.getLong("brand_id"));
                p.setImageUrl(rs.getString("image_url"));

                p.setOldPrice(rs.getObject("old_price", Double.class));
                return p;
            }, id);

            if (product != null) {

                loadProductDetails(product);
//                product.setGallery(jdbcTemplate.queryForList("SELECT image_url FROM elstore_product_images WHERE product_id = ?", String.class, id));
//
//                product.setGallery(new java.util.ArrayList<>());
//                product.setColors(new java.util.ArrayList<>());
//                product.setSpecifications(new java.util.HashMap<>());
            }

            return product;

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE elstore_products SET " +
                "name = ?, description = ?, price = ?, stock_quantity = ?, " +
                "category_id = ?, brand_id = ?, image_url = ? " +
                "WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategoryId(),
                product.getBrandId(),
                product.getImageUrl(),
                product.getId()
        );

        return rows > 0;
    }

    public boolean deleteProduct(Long id) {
        jdbcTemplate.update("DELETE FROM elstore_wishlist WHERE product_id = ?", id);

        jdbcTemplate.update("DELETE FROM elstore_product_specs WHERE product_id = ?", id);

        String sql = "DELETE FROM elstore_products WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    /**
     * Helper method to load related entities (images, specs, variants).
     * @param product The product object to populate.
     */
    private void loadProductDetails(Product product) {
        Long id = product.getId();

        // Gallery
        List<String> gallery = jdbcTemplate.queryForList(
                "SELECT image_url FROM elstore_product_images WHERE product_id = ?", String.class, id);
        if (product.getImageUrl() != null) gallery.add(0, product.getImageUrl());
        product.setGallery(gallery);

        // Specifications
        Map<String, String> specs = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT spec_key, spec_value FROM elstore_product_specs WHERE product_id = ? ORDER BY sort_order",
                (rs) -> {
                    specs.put(rs.getString("spec_key"), rs.getString("spec_value"));
                }, id);
        product.setSpecifications(specs);

        // Colors
        List<Product.ProductColor> colors = jdbcTemplate.query(
                "SELECT color_name, hex_value, quantity FROM elstore_product_colors WHERE product_id = ?",
                (rs, rn) -> new Product.ProductColor(rs.getString("color_name"), rs.getString("hex_value"), rs.getInt("quantity")), id);
        product.setColors(colors);

        // Memory Options
        List<Product.ProductMemory> memory = jdbcTemplate.query(
                "SELECT memory_size, price_modifier, quantity FROM elstore_product_memory WHERE product_id = ?",
                (rs, rn) -> new Product.ProductMemory(rs.getString("memory_size"), rs.getBigDecimal("price_modifier"), rs.getInt("quantity")), id);
        product.setMemoryOptions(memory);
    }

    /**
     * Creates a new product with all details (Full Create).
     * <p>
     * Uses {@code @Transactional} to ensure all inserts (product, images, specs)
     * succeed or fail together.
     * </p>
     *
     * @param product The product object to save.
     * @return true if created successfully, false otherwise.
     */
    @Transactional
    public boolean createProductFull(Product product) {
        String sql = "INSERT INTO elstore_products (name, description, price, old_price, stock_quantity, category_id, brand_id, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setObject(4, product.getOldPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setObject(6, product.getCategoryId());
            ps.setObject(7, product.getBrandId());
            ps.setString(8, product.getImageUrl());
            return ps;
        }, keyHolder);

        if (rows == 0 || keyHolder.getKey() == null) return false;
        long newId = keyHolder.getKey().longValue();

        // 1. Batch Insert: Colors
        if (product.getColors() != null && !product.getColors().isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT INTO elstore_product_colors (product_id, color_name, hex_value, quantity) VALUES (?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Product.ProductColor c = product.getColors().get(i);
                            ps.setLong(1, newId);
                            ps.setString(2, c.getName());
                            ps.setString(3, c.getHex());
                            ps.setInt(4, c.getQuantity());
                        }
                        public int getBatchSize() { return product.getColors().size(); }
                    });
        }

        // 2. Batch Insert: Specifications
        if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
            List<Map.Entry<String, String>> entries = new ArrayList<>(product.getSpecifications().entrySet());
            jdbcTemplate.batchUpdate("INSERT INTO elstore_product_specs (product_id, spec_key, spec_value, sort_order) VALUES (?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Map.Entry<String, String> entry = entries.get(i);
                            ps.setLong(1, newId);
                            ps.setString(2, entry.getKey());
                            ps.setString(3, entry.getValue());
                            ps.setInt(4, i + 1);
                        }
                        public int getBatchSize() { return entries.size(); }
                    });
        }

        // 3. Batch Insert: Gallery
        if (product.getGallery() != null && !product.getGallery().isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT INTO elstore_product_images (product_id, image_url) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, newId);
                            ps.setString(2, product.getGallery().get(i));
                        }
                        public int getBatchSize() { return product.getGallery().size(); }
                    });
        }

        return true;
    }
}