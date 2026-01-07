package org.store.dao;

import org.store.config.DBConnection;
import java.sql.*;

/**
 * Data Access Object (DAO) for managing promotional codes.
 * <p>
 * Handles interactions with the {@code elstore_promo_codes} table to retrieve
 * and validate discount coupons used during checkout.
 * </p>
 */
public class PromoCodeDAO {

    /**
     * Retrieves the discount percentage for a specific promotional code.
     * <p>
     * This method performs a database lookup to check if the provided code exists
     * AND is currently marked as active ({@code is_active = 1}).
     * </p>
     *
     * @param code The alphanumeric promo code entered by the user (e.g., "SUMMER2024").
     * @return The discount percentage as an integer (e.g., 10 for 10% off).
     * Returns {@code 0} if the code is invalid, expired, inactive, or not found.
     */
    public int getDiscount(String code) {
        String sql = "SELECT discount_percent FROM elstore_promo_codes WHERE code = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("discount_percent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Код не знайдено або він неактивний
    }
}