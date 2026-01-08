package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.store.model.Product;

import java.util.List;

/**
 * Data Access Object (DAO) for managing the user's Wishlist using Spring JDBC.
 */
@Repository
public class WishlistDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WishlistDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapper: перетворює рядок SQL (з таблиці продуктів) в об'єкт Product.
     */
    private final RowMapper<Product> productMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setImageUrl(rs.getString("image_url"));
        return p;
    };

    /**
     * Retrieves all products in user's wishlist.
     */
    public List<Product> getWishlist(Long userId) {
        String sql = "SELECT p.* FROM elstore_products p " +
                "JOIN elstore_wishlist w ON p.id = w.product_id " +
                "WHERE w.user_id = ?";
        return jdbcTemplate.query(sql, productMapper, userId);
    }

    /**
     * Adds a product to the wishlist.
     * Handles duplicates gracefully using exception handling.
     */
    public boolean addToWishlist(Long userId, Long productId) {
        String sql = "INSERT INTO elstore_wishlist (user_id, product_id) VALUES (?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, userId, productId);
            return rows > 0;
        } catch (DataIntegrityViolationException e) {
            // Вже є в списку бажань (дублікат) - це нормально, просто повертаємо false
            return false;
        }
    }

    /**
     * Removes a product from the wishlist.
     */
    public boolean removeFromWishlist(Long userId, Long productId) {
        String sql = "DELETE FROM elstore_wishlist WHERE user_id = ? AND product_id = ?";
        return jdbcTemplate.update(sql, userId, productId) > 0;
    }
}