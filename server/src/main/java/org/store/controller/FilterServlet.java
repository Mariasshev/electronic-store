package org.store.controller;

import com.google.gson.Gson;
import org.store.dao.ProductDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/filters")
public class FilterServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setContentType("application/json;charset=UTF-8");

        String catId = req.getParameter("categoryId");
        if (catId != null) {
            var filters = productDAO.getFiltersByCategory(Long.parseLong(catId));
            resp.getWriter().write(gson.toJson(filters));
        } else {
            resp.getWriter().write("{}");
        }
    }
}