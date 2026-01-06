package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.CartItem;
import org.store.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

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

    // Додати в корзину
    public boolean addToCart(Long userId, Long productId, int quantity) {
        // Перевіряємо, чи є вже такий товар (UPSERT логіка для Oracle трохи складна, зробимо простіше)
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

    // Видалити з корзини
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

    // Зміна кількості
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