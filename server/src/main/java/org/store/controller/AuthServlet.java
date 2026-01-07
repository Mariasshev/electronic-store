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
 * Servlet implementation class AuthServlet.
 * Handles authentication and registration requests via the endpoint {@code /api/auth/*}.
 * <p>
 * Supported routes:
 * <ul>
 * <li>{@code /api/auth/register} - Creates a new user account.</li>
 * <li>{@code /api/auth/login} - Authenticates existing users.</li>
 * </ul>
 * </p>
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public AuthServlet() {
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
    public AuthServlet(UserDAO userDAO, Gson gson) {
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
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
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
     * Handles POST requests for Registration and Login.
     * Uses {@code req.getPathInfo()} to determine the specific action.
     *
     * @param req  HttpServletRequest containing JSON payload (username, email, password).
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Читаем JSON
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

        String path = req.getPathInfo(); // /register или /login

        if ("/register".equals(path)) {
            // Логика регистрации
            User newUser = new User(
                    json.get("username").getAsString(),
                    json.get("email").getAsString(),
                    json.get("password").getAsString(),
                    "CLIENT"
            );

            if (userDAO.registerUser(newUser)) {
                resp.getWriter().write("{\"message\": \"Success\"}");
            } else {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\": \"User already exists\"}");
            }

        } else if ("/login".equals(path)) {
            // Логика входа
            String email = json.get("email").getAsString();
            String password = json.get("password").getAsString();

            User user = userDAO.loginUser(email, password);

            if (user != null) {
                // Отправляем данные юзера на фронт
                resp.getWriter().write(gson.toJson(user));
            } else {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\": \"Wrong email or password\"}");
            }
        }
    }
}