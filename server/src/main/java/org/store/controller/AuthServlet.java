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

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    // Настройка CORS (чтобы фронт не ругался)
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

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