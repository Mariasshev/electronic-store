package org.store.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.store.model.UserCard;

import java.util.List;

/**
 * Data Access Object (DAO) for managing User payment cards using Spring JDBC.
 */
@Repository
public class UserCardDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserCardDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserCard> cardMapper = (rs, rowNum) -> new UserCard(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("card_holder"),
            rs.getString("brand"),
            rs.getString("last4"),
            rs.getString("expiry_date")
    );

    /**
     * Retrieves all saved payment cards associated with a specific user.
     */
    public List<UserCard> getUserCards(Long userId) {
        String sql = "SELECT * FROM elstore_user_cards WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, cardMapper, userId);
    }

    /**
     * Saves a new card metadata (Last4, Brand).
     */
    public boolean addCard(UserCard card) {
        String sql = "INSERT INTO elstore_user_cards (user_id, card_holder, brand, last4, expiry_date) VALUES (?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                card.getUserId(),
                card.getCardHolder(),
                card.getBrand(),
                card.getLast4(),
                card.getExpiryDate()
        );
        return rows > 0;
    }

    /**
     * Removes a stored card.
     */
    public boolean deleteCard(Long cardId) {
        String sql = "DELETE FROM elstore_user_cards WHERE id = ?";
        return jdbcTemplate.update(sql, cardId) > 0;
    }
}