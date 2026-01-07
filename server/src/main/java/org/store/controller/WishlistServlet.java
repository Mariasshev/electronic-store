package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.WishlistDAO;
import org.store.model.Product;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class WishlistServlet.
 * Manages the user's list of favorite products via the endpoint {@code /api/wishlist}.
 * <p>
 * Supported operations:
 * <ul>
 * <li><b>GET:</b> Retrieve all products in the wishlist (requires {@code userId}).</li>
 * <li><b>POST:</b> Add a product to the wishlist (requires JSON body).</li>
 * <li><b>DELETE:</b> Remove a product (requires {@code userId} and {@code productId}).</li>
 * </ul>
 * </p>
 */
@WebServlet("/api/wishlist")
public class WishlistServlet extends HttpServlet {

    private WishlistDAO wishlistDAO;
    private Gson gson;

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public WishlistServlet() {
        this.wishlistDAO = new WishlistDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param wishlistDAO Mock or real WishlistDAO.
     * @param gson Mock or real Gson.
     */
    public WishlistServlet(WishlistDAO wishlistDAO, Gson gson) {
        this.wishlistDAO = wishlistDAO;
        this.gson = gson;
    }

    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     *
     * @param resp The HTTP response object.
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Handles CORS Preflight requests.
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handles GET requests to retrieve the wishlist.
     * Expects a {@code userId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code userId}.
     * @param resp HttpServletResponse containing the list of Products in JSON.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userIdParam = req.getParameter("userId");
        if (userIdParam == null) {
            resp.getWriter().write("[]");
            return;
        }

        try {
            long userId = Long.parseLong(userIdParam);
            List<Product> products = wishlistDAO.getWishlist(userId);
            resp.getWriter().write(gson.toJson(products));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid userId format\"}");
        }
    }

    /**
     * Handles POST requests to add a product to the wishlist.
     * Expects JSON body with {@code userId} and {@code productId}.
     *
     * @param req  HttpServletRequest containing JSON payload.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        try {
            JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
            if (!json.has("userId") || !json.has("productId")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            long userId = json.get("userId").getAsLong();
            long productId = json.get("productId").getAsLong();

            if (wishlistDAO.addToWishlist(userId, productId)) {
                resp.getWriter().write("{\"message\": \"Added to wishlist\"}");
            } else {
                resp.getWriter().write("{\"message\": \"Already in wishlist or failed\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid JSON\"}");
        }
    }

    /**
     * Handles DELETE requests to remove a product from the wishlist.
     * Expects {@code userId} and {@code productId} query parameters.
     *
     * @param req  HttpServletRequest containing query parameters.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        String userIdParam = req.getParameter("userId");
        String productIdParam = req.getParameter("productId");

        if (userIdParam != null && productIdParam != null) {
            try {
                long userId = Long.parseLong(userIdParam);
                long productId = Long.parseLong(productIdParam);

                if (wishlistDAO.removeFromWishlist(userId, productId)) {
                    resp.getWriter().write("{\"message\": \"Removed from wishlist\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Item not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing parameters\"}");
        }
    }
}