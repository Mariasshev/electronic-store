package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.store.model.Order;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) for managing customer Orders using Spring JDBC.
 * Annotated with {@code @Repository} for Spring detection.
 */
@Repository
public class OrderDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrderDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a new order based on the user's current shopping cart.
     * <p>
     * <b>Transaction Management:</b> The {@code @Transactional} annotation ensures
     * that all 3 steps (Create Order, Move Items, Clear Cart) happen atomically.
     * If any step fails, Spring automatically rolls back the changes.
     * </p>
     *
     * @param userId      The ID of the user placing the order.
     * @param address     The shipping address.
     * @param phone       The contact phone number.
     * @param totalAmount The total cost of the order.
     * @return true if successful (exception thrown otherwise).
     */
    @Transactional
    public boolean createOrder(Long userId, String address, String phone, BigDecimal totalAmount) {
        String sqlOrder = "INSERT INTO elstore_orders (user_id, total_price, shipping_address, phone, status) VALUES (?, ?, ?, ?, 'PAID')";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlOrder, new String[]{"id"});
            ps.setLong(1, userId);
            ps.setBigDecimal(2, totalAmount);
            ps.setString(3, address);
            ps.setString(4, phone);
            return ps;
        }, keyHolder);

        if (rows == 0 || keyHolder.getKey() == null) {
            throw new RuntimeException("Failed to create order");
        }

        long orderId = keyHolder.getKey().longValue();

        // 2. Копіюємо товари з Кошик в OrderItems
        String sqlMoveItems =
                "INSERT INTO elstore_order_items (order_id, product_id, quantity, price_at_purchase) " +
                        "SELECT ?, c.product_id, c.quantity, p.price " +
                        "FROM elstore_cart c " +
                        "JOIN elstore_products p ON c.product_id = p.id " +
                        "WHERE c.user_id = ?";

        jdbcTemplate.update(sqlMoveItems, orderId, userId);

        // 3. Очищаємо кошик
        String sqlClearCart = "DELETE FROM elstore_cart WHERE user_id = ?";
        jdbcTemplate.update(sqlClearCart, userId);

        return true;
    }

    /**
     * Retrieves the order history for a specific user.
     * Sorted by creation date descending.
     */
    public List<Order> getUserOrders(Long userId) {
        String sql = "SELECT * FROM elstore_orders WHERE user_id = ? ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setTotalPrice(rs.getBigDecimal("total_price"));
            order.setStatus(rs.getString("status"));
            order.setCreatedAt(rs.getTimestamp("created_at"));

            order.setItems(getOrderItems(order.getId()));
            return order;
        }, userId);
    }

    /**
     * Helper method to retrieve items for a specific order.
     */
    private List<Order.OrderItem> getOrderItems(Long orderId) {
        String sql = "SELECT oi.quantity, oi.price_at_purchase, p.name, p.image_url " +
                "FROM elstore_order_items oi " +
                "JOIN elstore_products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Order.OrderItem(
                rs.getString("name"),
                rs.getInt("quantity"),
                rs.getBigDecimal("price_at_purchase"),
                rs.getString("image_url")
        ), orderId);
    }
}