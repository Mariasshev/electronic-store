package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.PromoCodeDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/promo")
public class PromoCodeServlet extends HttpServlet {
    private PromoCodeDAO promoDAO = new PromoCodeDAO();
    private Gson gson = new Gson();

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
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

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        String code = json.get("code").getAsString();

        int discount = promoDAO.getDiscount(code);

        JsonObject responseJson = new JsonObject();
        if (discount > 0) {
            responseJson.addProperty("valid", true);
            responseJson.addProperty("discountPercent", discount);
            resp.getWriter().write(gson.toJson(responseJson));
        } else {
            responseJson.addProperty("valid", false);
            resp.getWriter().write(gson.toJson(responseJson));
        }
    }
}