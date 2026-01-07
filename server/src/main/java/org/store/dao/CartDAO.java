package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.CartItem;
import org.store.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) for managing the user's Shopping Cart.
 * <p>
 * This class handles interactions with the {@code elstore_cart} table.
 * It supports adding items, updating quantities, removing items, and retrieving
 * the full cart with joined product details.
 * </p>
 */
public class CartDAO {


    /**
     * Retrieves the current contents of a user's shopping cart.
     * <p>
     * Performs a SQL JOIN with {@code elstore_products} to immediately fetch
     * product details (name, price, image) alongside the cart quantity.
     * </p>
     *
     * @param userId The ID of the user whose cart is being requested.
     * @return A List of {@link CartItem} objects populated with product data.
     */
    public List<CartItem> getCart(Long userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT c.id as cart_id, c.quantity, p.* FROM elstore_cart c " +
                "JOIN elstore_products p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";

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

                CartItem item = new CartItem();
                item.setId(rs.getLong("cart_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setProduct(p);

                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Adds a product to the cart or increments its quantity if it already exists.
     * <p>
     * Logic:
     * 1. Checks if the product is already in the user's cart.
     * 2. If YES: Updates the existing record by adding the new quantity to the old one.
     * 3. If NO: Inserts a new record into {@code elstore_cart}.
     * </p>
     *
     * @param userId    The user ID.
     * @param productId The product ID to add.
     * @param quantity  The amount to add.
     * @return true if the operation was successful.
     */
    public boolean addToCart(Long userId, Long productId, int quantity) {
        // Перевіряємо, чи є вже такий товар
        String checkSql = "SELECT id, quantity FROM elstore_cart WHERE user_id = ? AND product_id = ?";
        String updateSql = "UPDATE elstore_cart SET quantity = quantity + ? WHERE id = ?";
        String insertSql = "INSERT INTO elstore_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Перевірка
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, userId);
                checkStmt.setLong(2, productId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // 2. Якщо є - оновлюємо кількість
                    long cartId = rs.getLong("id");
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setLong(2, cartId);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }

            // 3. Якщо немає - вставляємо
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setLong(1, userId);
                insertStmt.setLong(2, productId);
                insertStmt.setInt(3, quantity);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a specific product from the user's cart completely.
     *
     * @param userId    The user ID.
     * @param productId The product ID to remove.
     * @return true if the item was found and deleted.
     */
    public boolean removeFromCart(Long userId, Long productId) {
        String sql = "DELETE FROM elstore_cart WHERE user_id = ? AND product_id = ?";
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

    /**
     * Updates the absolute quantity of a product in the cart.
     * <p>
     * Special handling: If {@code newQuantity} is less than or equal to 0,
     * the item is removed from the cart instead of updating it.
     * </p>
     *
     * @param userId      The user ID.
     * @param productId   The product ID.
     * @param newQuantity The new specific quantity to set (e.g., set to 5, not add 5).
     * @return true if the update (or deletion) was successful.
     */
    public boolean updateQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(userId, productId); // Якщо 0, то видаляємо
        }
        String sql = "UPDATE elstore_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setLong(2, userId);
            pstmt.setLong(3, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}