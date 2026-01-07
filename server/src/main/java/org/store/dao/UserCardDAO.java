package org.store.dao;

import org.store.config.DBConnection;
import org.store.model.UserCard;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing User payment cards.
 * <p>
 * This class handles database operations for the {@code elstore_user_cards} table.
 * It allows users to view their saved cards, add new ones, and remove them.
 * </p>
 * <p>
 * <b>Security Note:</b> This DAO never handles full credit card numbers (PAN).
 * It only stores and retrieves the "Last 4" digits, brand, and holder name for display purposes.
 * </p>
 */
public class UserCardDAO {

    /**
     * Retrieves all saved payment cards associated with a specific user.
     * The results are ordered by creation date in descending order (newest first).
     *
     * @param userId The unique identifier of the user.
     * @return A list of {@link UserCard} objects containing card metadata (brand, last4, etc.).
     */
    public List<UserCard> getUserCards(Long userId) {
        List<UserCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM elstore_user_cards WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UserCard card = new UserCard();
                card.setId(rs.getLong("id"));
                card.setCardHolder(rs.getString("card_holder"));
                card.setBrand(rs.getString("brand"));
                card.setLast4(rs.getString("last4"));
                card.setExpiryDate(rs.getString("expiry_date"));
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * Saves a new card to the user's profile.
     * <p>
     * Typically called after a successful payment or when the user chooses "Save card".
     * Only non-sensitive data (masked number) is stored.
     * </p>
     *
     * @param card The {@link UserCard} object containing the card details.
     * @return true if the card was successfully saved; false otherwise.
     */
    public boolean addCard(UserCard card) {
        String sql = "INSERT INTO elstore_user_cards (user_id, card_holder, brand, last4, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, card.getUserId());
            pstmt.setString(2, card.getCardHolder());
            pstmt.setString(3, card.getBrand());
            pstmt.setString(4, card.getLast4());
            pstmt.setString(5, card.getExpiryDate());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a stored card from the database.
     *
     * @param cardId The unique identifier of the card record to delete.
     * @return true if the card was successfully deleted; false otherwise.
     */
    public boolean deleteCard(Long cardId) {
        String sql = "DELETE FROM elstore_user_cards WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}