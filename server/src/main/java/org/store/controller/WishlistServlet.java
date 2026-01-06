package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.config.DBConnection;
import org.store.model.Product;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/wishlist")
public class WishlistServlet extends HttpServlet {

    private Gson gson = new Gson();

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    // GET: Отримати всі ТОВАРИ з вішлиста користувача
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userIdParam = req.getParameter("userId");
        if (userIdParam == null) { resp.getWriter().write("[]"); return; }

        List<Product> products = new ArrayList<>();

        String sql = "SELECT p.* FROM elstore_products p " +
                "JOIN elstore_wishlist w ON p.id = w.product_id " +
                "WHERE w.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, Long.parseLong(userIdParam));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setImageUrl(rs.getString("image_url"));
                products.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.getWriter().write(gson.toJson(products));
    }


    // POST: Додати товар у вішлист
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        long userId = json.get("userId").getAsLong();
        long productId = json.get("productId").getAsLong();

        String sql = "INSERT INTO elstore_wishlist (user_id, product_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, productId);
            pstmt.executeUpdate();

            resp.getWriter().write("{\"message\": \"Added to wishlist\"}");

        } catch (SQLIntegrityConstraintViolationException e) {
            // Якщо товар вже є у вішлисті - повертаємо ОК
            resp.getWriter().write("{\"message\": \"Already in wishlist\"}");
        } catch (SQLException e) {
            resp.setStatus(500);
            e.printStackTrace();
        }
    }

    // DELETE: Видалити товар з вішлиста
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        String userId = req.getParameter("userId");
        String productId = req.getParameter("productId");

        if (userId == null || productId == null) {
            resp.setStatus(400);
            return;
        }

        String sql = "DELETE FROM elstore_wishlist WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, Long.parseLong(userId));
            pstmt.setLong(2, Long.parseLong(productId));
            pstmt.executeUpdate();

            resp.getWriter().write("{\"message\": \"Removed from wishlist\"}");
        } catch (SQLException e) {
            resp.setStatus(500);
            e.printStackTrace();
        }
    }
}