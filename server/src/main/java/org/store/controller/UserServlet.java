package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.UserDAO;
import org.store.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/user") // REST шлях
public class UserServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    // Налаштування CORS
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    // ОТРИМАННЯ ДАНИХ GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String email = req.getParameter("email");

        if (email == null || email.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Email parameter is required\"}");
            return;
        }

        User user = userDAO.findByEmail(email);

        if (user != null) {
            // Видаляємо пароль перед відправкою на клієнт
            user.setPassword(null);
            resp.getWriter().write(gson.toJson(user));
        } else {
            resp.setStatus(404);
            resp.getWriter().write("{\"error\": \"User not found\"}");
        }
    }

    // ОНОВЛЕННЯ ДАНИХ PUT
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

        // --- ЛОГІКА ЗМІНИ ПАРОЛЯ ---
        if (json.has("newPassword") && json.has("currentPassword")) {
            String email = json.get("email").getAsString();
            String currentPass = json.get("currentPassword").getAsString();
            String newPass = json.get("newPassword").getAsString();

            if (userDAO.changePassword(email, currentPass, newPass)) {
                resp.getWriter().write("{\"message\": \"Password changed successfully\"}");
            } else {
                resp.setStatus(400); // Bad Request
                resp.getWriter().write("{\"error\": \"Incorrect current password\"}");
            }
            return;
        }

        // --- ЛОГІКА ОНОВЛЕННЯ ПРОФІЛЮ ---
        try {
            User userToUpdate = gson.fromJson(json, User.class);

            if (userDAO.updateUser(userToUpdate)) {
                User updatedUser = userDAO.findByEmail(userToUpdate.getEmail());
                updatedUser.setPassword(null);
                resp.getWriter().write(gson.toJson(updatedUser));
            } else {
                resp.setStatus(500);
                resp.getWriter().write("{\"error\": \"Update failed\"}");
            }
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Invalid data format\"}");
        }
    }
}