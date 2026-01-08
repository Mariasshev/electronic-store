package org.store.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.store.service.CurrencyService;
import java.io.IOException;

@WebServlet("/api/currency")
public class CurrencyController extends HttpServlet {

    private CurrencyService currencyService;
    private Gson gson;

    public CurrencyController() {
        this.currencyService = new CurrencyService();
        this.gson = new Gson();
    }

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            var rates = this.currencyService.getExchangeRates();

            String jsonOutput = this.gson.toJson(rates);
            resp.getWriter().write(jsonOutput);

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("[]");
        }
    }
}