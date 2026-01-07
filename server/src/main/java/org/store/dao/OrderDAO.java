package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.Order;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing customer Orders.
 * <p>
 * This class handles the complex logic of the checkout process, including
 * transaction management to ensure data integrity when moving items
 * from the shopping cart to the final order records.
 * </p>
 */
public class OrderDAO {

    /**
     * Creates a new order based on the user's current shopping cart.
     * <p>
     * <b>Transaction Management:</b> This method executes as a single atomic transaction.
     * It performs three steps:
     * <ol>
     * <li>Creates a record in {@code elstore_orders} with status 'PAID'.</li>
     * <li>Copies all items from {@code elstore_cart} to {@code elstore_order_items}, preserving the price at the moment of purchase.</li>
     * <li>Clears the user's shopping cart ({@code elstore_cart}).</li>
     * </ol>
     * If any step fails, the entire operation is rolled back to prevent data inconsistency.
     * </p>
     *
     * @param userId      The ID of the user placing the order.
     * @param address     The shipping address.
     * @param phone       The contact phone number.
     * @param totalAmount The total cost of the order (calculated on the server or client).
     * @return true if the order was successfully created and the cart cleared; false otherwise.
     */
    public boolean createOrder(Long userId, String address, String phone, BigDecimal totalAmount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Починаємо транзакцію

            // 1. Створюємо запис в elstore_orders
            String sqlOrder = "INSERT INTO elstore_orders (user_id, total_price, shipping_address, phone, status) VALUES (?, ?, ?, ?, 'PAID')";
            long orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder, new String[]{"id"})) {
                pstmt.setLong(1, userId);
                pstmt.setBigDecimal(2, totalAmount);
                pstmt.setString(3, address);
                pstmt.setString(4, phone);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) orderId = rs.getLong(1);
                    else throw new SQLException("Failed to create order");
                }
            }

            // 2. Копіюємо товари з Корзини в OrderItems
            // Ми беремо дані прямо з elstore_cart + elstore_products
            String sqlMoveItems =
                    "INSERT INTO elstore_order_items (order_id, product_id, quantity, price_at_purchase) " +
                            "SELECT ?, c.product_id, c.quantity, p.price " +
                            "FROM elstore_cart c " +
                            "JOIN elstore_products p ON c.product_id = p.id " +
                            "WHERE c.user_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlMoveItems)) {
                pstmt.setLong(1, orderId);
                pstmt.setLong(2, userId);
                pstmt.executeUpdate();
            }

            // 3. Очищаємо корзину
            String sqlClearCart = "DELETE FROM elstore_cart WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlClearCart)) {
                pstmt.setLong(1, userId);
                pstmt.executeUpdate();
            }

            conn.commit(); // Все успішно - зберігаємо
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Retrieves the order history for a specific user.
     * The list is sorted by creation date in descending order (newest orders first).
     *
     * @param userId The ID of the user.
     * @return A List of {@link Order} objects, each containing its associated list of purchased items.
     */
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM elstore_orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setTotalPrice(rs.getBigDecimal("total_price"));
                order.setStatus(rs.getString("status"));
                order.setCreatedAt(rs.getTimestamp("created_at"));

                order.setItems(getOrderItems(order.getId(), conn));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Helper method to retrieve individual items associated with a specific order.
     * Joins with the products table to get current names and images.
     *
     * @param orderId The ID of the order.
     * @param conn    The active database connection (reused from the parent method).
     * @return A list of {@link Order.OrderItem} containing product details and purchase price.
     * @throws SQLException If a database access error occurs.
     */
    private List<Order.OrderItem> getOrderItems(Long orderId, Connection conn) throws SQLException {
        List<Order.OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.quantity, oi.price_at_purchase, p.name, p.image_url " +
                "FROM elstore_order_items oi " +
                "JOIN elstore_products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                items.add(new Order.OrderItem(
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("price_at_purchase"),
                        rs.getString("image_url")
                ));
            }
        }
        return items;
    }
}