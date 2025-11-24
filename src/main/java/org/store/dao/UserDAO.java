package org.store.dao;

import org.store.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // CREATE
    public void addUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());

            stmt.executeUpdate();
            System.out.println("User added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void updateUserName(int id, String newName) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("User updated!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("User deleted!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
