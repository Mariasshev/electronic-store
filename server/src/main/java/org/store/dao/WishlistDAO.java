package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing the user's Wishlist.
 * <p>
 * Handles database operations for the {@code elstore_wishlist} table,
 * including adding products, removing them, and retrieving the list of liked items.
 * </p>
 */
public class WishlistDAO {

    /**
     * Retrieves all products currently in a specific user's wishlist.
     * Performs a JOIN with the {@code elstore_products} table to get product details.
     *
     * @param userId The unique identifier of the user.
     * @return A List of {@link Product} objects. Returns empty list if no items found.
     */
    public List<Product> getWishlist(Long userId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM elstore_products p " +
                "JOIN elstore_wishlist w ON p.id = w.product_id " +
                "WHERE w.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
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

    /**
     * Adds a product to the user's wishlist.
     * Handles duplicates gracefully (if the pair user_id + product_id already exists).
     *
     * @param userId    The user ID.
     * @param productId The product ID to add.
     * @return {@code true} if added successfully, {@code false} if already exists or error.
     */
    public boolean addToWishlist(Long userId, Long productId) {
        String sql = "INSERT INTO elstore_wishlist (user_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, productId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Product already in wishlist - treat as safe operation
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a product from the user's wishlist.
     *
     * @param userId    The user ID.
     * @param productId The product ID to remove.
     * @return {@code true} if removed successfully.
     */
    public boolean removeFromWishlist(Long userId, Long productId) {
        String sql = "DELETE FROM elstore_wishlist WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}