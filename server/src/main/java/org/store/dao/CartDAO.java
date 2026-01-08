package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.store.model.CartItem;
import org.store.model.Product;

import java.util.List;

@Repository
public class CartDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CartDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private final RowMapper<CartItem> cartItemMapper = (rs, rowNum) -> {
        // 1. об'єкт Product
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setImageUrl(rs.getString("image_url"));

        // 2. Створюємо CartItem
        CartItem item = new CartItem();
        item.setId(rs.getLong("cart_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setProduct(p);

        return item;
    };

    /**
     * Get user's cart with product details.
     */
    public List<CartItem> getCart(Long userId) {
        String sql = "SELECT c.id as cart_id, c.quantity, p.* FROM elstore_cart c " +
                "JOIN elstore_products p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";
        return jdbcTemplate.query(sql, cartItemMapper, userId);
    }

    /**
     * Add to cart: Update quantity if exists, otherwise Insert.
     */
    public boolean addToCart(Long userId, Long productId, int quantity) {
        String checkSql = "SELECT id FROM elstore_cart WHERE user_id = ? AND product_id = ?";

        try {
            Long existingCartId = jdbcTemplate.queryForObject(checkSql, Long.class, userId, productId);

            if (existingCartId != null) {
                // UPDATE
                String updateSql = "UPDATE elstore_cart SET quantity = quantity + ? WHERE id = ?";
                jdbcTemplate.update(updateSql, quantity, existingCartId);
            }
        } catch (EmptyResultDataAccessException e) {
            String insertSql = "INSERT INTO elstore_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, productId, quantity);
        }
        return true;
    }

    /**
     * Remove specific item.
     */
    public boolean removeFromCart(Long userId, Long productId) {
        String sql = "DELETE FROM elstore_cart WHERE user_id = ? AND product_id = ?";
        return jdbcTemplate.update(sql, userId, productId) > 0;
    }

    /**
     * Set specific quantity. If <= 0, remove item.
     */
    public boolean updateQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(userId, productId);
        }
        String sql = "UPDATE elstore_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        return jdbcTemplate.update(sql, newQuantity, userId, productId) > 0;
    }
}