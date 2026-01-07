package org.store.dao;

import org.mindrot.jbcrypt.BCrypt;
import org.store.config.DBConnection;
import org.store.model.User;

import java.sql.*;

/**
 * Data Access Object (DAO) for managing User entities.
 * Handles database operations related to user registration, authentication, and profile management.
 * <p>
 * Security Note:
 * Passwords are never stored in plain text. This class uses the BCrypt algorithm
 * to hash passwords during registration and verify them during login.
 * </p>
 */
public class UserDAO {

    /**
     * Registers a new user in the database.
     * <p>
     * Implementation details:
     * 1. Generates a salt and hashes the user's password using BCrypt.
     * 2. Inserts the user into the 'elstore_users' table with the default role 'CLIENT'.
     * </p>
     *
     * @param user The {@link User} object containing username, email, and raw password.
     * @return true if the user was successfully registered; false otherwise (e.g., if email already exists).
     */
    public boolean registerUser(User user) {
        // 1. Генерируем соль и хешируем пароль
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        // 2. Вставляем в базу
        String sql = "INSERT INTO elstore_users (username, email, password, role) VALUES (?, ?, ?, 'CLIENT')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, hashedPassword);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates a user by email and password.
     * <p>
     * It fetches the user by email and uses {@link BCrypt#checkpw(String, String)}
     * to verify if the provided raw password matches the stored hash.
     * </p>
     *
     * @param email       The user's email address.
     * @param rawPassword The plain text password entered by the user.
     * @return A {@link User} object (excluding the password) if authentication is successful; null otherwise.
     */
    public User loginUser(String email, String rawPassword) {
        String sql = "SELECT * FROM elstore_users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // 3. проверка пароля с хешем из базы
                if (BCrypt.checkpw(rawPassword, storedHash)) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    // Пароль в объект не кладем, он не нужен на фронте
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Пользователь не найден или пароль неверный
    }

    /**
     * Retrieves full user details by email.
     * Used for loading profile information (address, phone, etc.).
     *
     * @param email The email address to search for.
     * @return A {@link User} object with populated fields, or null if not found.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM elstore_users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates user profile information.
     * Allows changing the username, address, and phone number.
     * Note: Email is used as the key and cannot be changed via this method.
     *
     * @param user The {@link User} object containing updated info.
     * @return true if the update was successful.
     */
    public boolean updateUser(User user) {
        // Оновлюємо ім'я, адресу та телефон
        String sql = "UPDATE elstore_users SET username = ?, address = ?, phone = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getAddress());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getEmail());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Securely changes the user's password.
     * <p>
     * Workflow:
     * 1. Fetches the current password hash from the database.
     * 2. Verifies that the provided {@code oldPassword} matches the current hash.
     * 3. If correct, hashes the {@code newPassword} and updates the database record.
     * </p>
     *
     * @param email       The user's email.
     * @param oldPassword The current password (plain text) for verification.
     * @param newPassword The new password (plain text) to be set.
     * @return true if the password was successfully changed; false if the old password was incorrect or an error occurred.
     */
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // 1. Спочатку дістаємо поточний хеш пароля з БД
        String sqlSelect = "SELECT password FROM elstore_users WHERE email = ?";
        String sqlUpdate = "UPDATE elstore_users SET password = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect)) {

            pstmtSelect.setString(1, email);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                String currentHash = rs.getString("password");

                // 2. Перевіряємо, чи співпадає старий пароль
                if (BCrypt.checkpw(oldPassword, currentHash)) {

                    // 3. хешуємо новий пароль і оновлюємо БД
                    String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                        pstmtUpdate.setString(1, newHash);
                        pstmtUpdate.setString(2, email);
                        return pstmtUpdate.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Помилка або старий пароль невірний
    }
}