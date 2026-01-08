package org.store.controller;

import com.google.gson.Gson;
import org.store.dao.ProductDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Servlet implementation class FilterController.
 * Serves dynamic filter options via the endpoint {@code /api/filters}.
 * <p>
 * This servlet is used by the frontend to populate the sidebar filters (Brands, Specifications)
 * based on the currently selected category.
 * </p>
 */
@WebServlet("/api/filters")
public class FilterController extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public FilterController() {
        this.productDAO = new ProductDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param productDAO Mock or real ProductDAO.
     * @param gson Mock or real Gson.
     */
    public FilterController(ProductDAO productDAO, Gson gson) {
        this.productDAO = productDAO;
        this.gson = gson;
    }


    /**
     * Handles GET requests to retrieve available filters for a category.
     * Expects a {@code categoryId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code categoryId}.
     * @param resp HttpServletResponse containing the FilterDTO JSON (brands and specs maps).
     * @throws IOException If an I/O error occurs.
     */
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