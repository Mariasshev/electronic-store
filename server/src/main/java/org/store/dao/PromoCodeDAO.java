package org.store.dao;

import org.store.config.DBConnection;
import java.sql.*;

public class PromoCodeDAO {
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
        return 0; // Код не знайдено
    }
}