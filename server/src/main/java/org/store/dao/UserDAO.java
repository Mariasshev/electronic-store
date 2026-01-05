package org.store.dao;

import org.mindrot.jbcrypt.BCrypt;
import org.store.config.DBConnection; // Твой класс подключения к Oracle
import org.store.model.User;

import java.sql.*;

public class UserDAO {

    // --- РЕГИСТРАЦИЯ ---
    public boolean registerUser(User user) {
        // 1. Генерируем соль и хешируем пароль
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        // 2. Вставляем в базу (ID генерируется сам, CREATED_AT тоже)
        String sql = "INSERT INTO elstore_users (username, email, password, role) VALUES (?, ?, ?, 'CLIENT')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, hashedPassword); // В базу пишем ТОЛЬКО хеш

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Скорее всего такой email уже есть
        }
    }

    // --- ВХОД (LOGIN) ---
    public User loginUser(String email, String rawPassword) {
        String sql = "SELECT * FROM elstore_users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // 3. Сверяем введенный пароль с хешем из базы
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

                // --- ДОДАНО: Читаємо телефон та адресу ---
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                // ----------------------------------------

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. ОНОВЛЕННЯ ПРОФІЛЮ (Для сторінки "Edit Profile")
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

                    // 3. Якщо так - хешуємо новий пароль і оновлюємо БД
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