package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Object (DAO) for managing promotional codes using Spring JDBC.
 */
@Repository
public class PromoCodeDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PromoCodeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Retrieves the discount percentage for a specific promotional code.
     * Returns 0 if code is invalid or inactive.
     */
    public int getDiscount(String code) {
        String sql = "SELECT discount_percent FROM elstore_promo_codes WHERE code = ? AND is_active = 1";

        List<Integer> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("discount_percent"),
                code
        );

        return result.isEmpty() ? 0 : result.get(0);
    }
}