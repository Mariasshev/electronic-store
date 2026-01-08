package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) for managing User Shipping Addresses.
 * <p>
 * This class handles CRUD operations for the {@code elstore_addresses} table.
 * It allows users to store multiple delivery addresses (e.g., "Home", "Work")
 * to speed up the checkout process.
 * </p>
 */
public class AddressDAO {

    /**
     * Retrieves all saved addresses for a specific user.
     * The results are ordered by ID in descending order, so the most recently
     * added address appears first in the list.
     *
     * @param userId The unique identifier of the user.
     * @return A {@link List} of {@link Address} objects. Returns an empty list if no addresses are found.
     */
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

    /**
     * Adds a new shipping address to the user's profile.
     *
     * @param addr The {@link Address} object containing details (label, address line, phone).
     * @return {@code true} if the address was successfully inserted into the database; {@code false} otherwise.
     */
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



    /**
     * Permanently removes an address from the user's profile.
     *
     * @param addressId The unique identifier (Primary Key) of the address to delete.
     * @return {@code true} if the deletion was successful (i.e., the row existed); {@code false} otherwise.
     */
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