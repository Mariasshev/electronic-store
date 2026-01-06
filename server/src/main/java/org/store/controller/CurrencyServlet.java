package org.store.controller;

import com.google.gson.Gson;
import org.store.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@WebServlet("/api/currency")
public class CurrencyServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // CORS
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET");

        resp.setContentType("application/json;charset=UTF-8");

        try {
            CurrencyService service = new CurrencyService();
            var rates = service.getExchangeRates();
            String jsonOutput = gson.toJson(rates);

            resp.getWriter().write(jsonOutput);

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("[]");
        }
    }
}