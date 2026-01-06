package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.UserCardDAO;
import org.store.model.UserCard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/cards")
public class UserCardServlet extends HttpServlet {
    private UserCardDAO cardDAO = new UserCardDAO();
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

    // GET: Отримати картки
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userId = req.getParameter("userId");
        if (userId != null) {
            List<UserCard> cards = cardDAO.getUserCards(Long.parseLong(userId));
            resp.getWriter().write(gson.toJson(cards));
        }
    }

    // POST: Додати картку
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

        // бренд
        String number = json.get("cardNumber").getAsString().replaceAll("\\s+", "");
        String brand = "MASTERCARD";
        if (number.startsWith("4")) brand = "VISA";

        // Беремо останні 4 цифри
        String last4 = number.length() >= 4 ? number.substring(number.length() - 4) : number;

        UserCard card = new UserCard(
                json.get("userId").getAsLong(),
                json.get("cardHolder").getAsString(),
                brand,
                last4,
                json.get("expiryDate").getAsString()
        );

        if (cardDAO.addCard(card)) {
            resp.getWriter().write("{\"message\": \"Card added\"}");
        } else {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"Failed to add card\"}");
        }
    }

    // DELETE: Видалити картку
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        String idParam = req.getParameter("id");
        if (idParam != null && cardDAO.deleteCard(Long.parseLong(idParam))) {
            resp.getWriter().write("{\"message\": \"Card deleted\"}");
        } else {
            resp.setStatus(500);
        }
    }
}