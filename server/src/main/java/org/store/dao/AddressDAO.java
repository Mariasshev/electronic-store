package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.store.model.Address;

import java.util.List;

@Repository
public class AddressDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AddressDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Address> addressMapper = (rs, rowNum) -> new Address(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("label"),
            rs.getString("address_line"),
            rs.getString("phone")
    );

    /**
     * Get user addresses ordered by ID descending.
     */
    public List<Address> findByUserId(Long userId) {
        String sql = "SELECT * FROM elstore_addresses WHERE user_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, addressMapper, userId);
    }

    /**
     * Add new address.
     */
    public boolean addAddress(Address addr) {
        String sql = "INSERT INTO elstore_addresses (user_id, label, address_line, phone) VALUES (?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                addr.getUserId(),
                addr.getLabel(),
                addr.getAddressLine(),
                addr.getPhone()
        );
        return rows > 0;
    }

    /**
     * Delete address by ID.
     */
    public boolean deleteAddress(Long addressId) {
        String sql = "DELETE FROM elstore_addresses WHERE id = ?";
        return jdbcTemplate.update(sql, addressId) > 0;
    }
}