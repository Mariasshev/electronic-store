package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    // Отримати всі адреси конкретного користувача
    public List<Address> findByUserId(Long userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT * FROM elstore_addresses WHERE user_id = ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Address a = new Address();
                a.setId(rs.getLong("id"));
                a.setUserId(rs.getLong("user_id"));
                a.setLabel(rs.getString("label"));
                a.setAddressLine(rs.getString("address_line"));
                a.setPhone(rs.getString("phone"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Додати нову адресу
    public boolean addAddress(Address addr) {
        String sql = "INSERT INTO elstore_addresses (user_id, label, address_line, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, addr.getUserId());
            pstmt.setString(2, addr.getLabel());
            pstmt.setString(3, addr.getAddressLine());
            pstmt.setString(4, addr.getPhone());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Видалити адресу
    public boolean deleteAddress(Long addressId) {
        String sql = "DELETE FROM elstore_addresses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, addressId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}