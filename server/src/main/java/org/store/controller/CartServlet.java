package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.CartDAO;
import org.store.model.CartItem;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {
    private CartDAO cartDAO = new CartDAO();
    private Gson gson = new Gson();

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userId = req.getParameter("userId");
        if (userId != null) {
            List<CartItem> items = cartDAO.getCart(Long.parseLong(userId));
            resp.getWriter().write(gson.toJson(items));
        } else {
            resp.getWriter().write("[]");
        }
    }

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
        int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 1;

        if (cartDAO.addToCart(userId, productId, quantity)) {
            resp.getWriter().write("{\"message\": \"Added to cart\"}");
        } else {
            resp.setStatus(500);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        String userId = req.getParameter("userId");
        String productId = req.getParameter("productId");

        if (cartDAO.removeFromCart(Long.parseLong(userId), Long.parseLong(productId))) {
            resp.getWriter().write("{\"message\": \"Removed\"}");
        } else {
            resp.setStatus(500);
        }
    }

    // PUT: Оновити кількість
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        long userId = json.get("userId").getAsLong();
        long productId = json.get("productId").getAsLong();
        int quantity = json.get("quantity").getAsInt();

        if (cartDAO.updateQuantity(userId, productId, quantity)) {
            resp.getWriter().write("{\"message\": \"Quantity updated\"}");
        } else {
            resp.setStatus(500);
        }
    }
}