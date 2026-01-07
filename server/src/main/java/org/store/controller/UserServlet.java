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

/**
 * Servlet implementation class UserServlet.
 * Manages user profile operations at the endpoint {@code /api/user}.
 * Supported operations:
 * <ul>
 * <li>GET: Retrieve user profile details by email.</li>
 * <li>PUT: Update user profile information OR change password.</li>
 * <li>OPTIONS: Handle CORS preflight requests.</li>
 * </ul>
 */
@WebServlet("/api/user")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private Gson gson;

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public UserServlet() {
        this.userDAO = new UserDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param userDAO Mock or real UserDAO.
     * @param gson Mock or real Gson.
     */
    public UserServlet(UserDAO userDAO, Gson gson) {
        this.userDAO = userDAO;
        this.gson = gson;
    }

    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     * Allows requests from {@code http://localhost:3000} (React Frontend).
     *
     * @param resp The HTTP response object.
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Handles CORS Preflight requests.
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    /**
     * Handles GET requests to retrieve user information.
     * Expects an 'email' query parameter.
     * <p>
     * Note: The password field is set to null before sending the response for security reasons.
     * </p>
     *
     * @param req  HttpServletRequest containing the 'email' parameter.
     * @param resp HttpServletResponse containing the User JSON or error message.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String email = req.getParameter("email");

        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write("{\"error\": \"Email parameter is required\"}");
            return;
        }

        User user = userDAO.findByEmail(email);

        if (user != null) {
            // Видаляємо пароль перед відправкою на клієнт
            user.setPassword(null);
            resp.getWriter().write(gson.toJson(user));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            resp.getWriter().write("{\"error\": \"User not found\"}");
        }
    }

    /**
     * Handles PUT requests for updating user data.
     * This method supports two distinct operations based on the JSON payload:
     * <ol>
     * <li><b>Password Change:</b> If payload contains {@code currentPassword} and {@code newPassword}.</li>
     * <li><b>Profile Update:</b> If payload contains standard user fields (address, phone, name).</li>
     * </ol>
     *
     * @param req  HttpServletRequest containing the JSON payload.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

        // --- ЛОГІКА ЗМІНИ ПАРОЛЯ ---
        if (json.has("newPassword") && json.has("currentPassword")) {
            String email = json.get("email").getAsString();
            String currentPass = json.get("currentPassword").getAsString();
            String newPass = json.get("newPassword").getAsString();

            if (userDAO.changePassword(email, currentPass, newPass)) {
                resp.getWriter().write("{\"message\": \"Password changed successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
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
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                resp.getWriter().write("{\"error\": \"Update failed\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write("{\"error\": \"Invalid data format\"}");
        }
    }
}