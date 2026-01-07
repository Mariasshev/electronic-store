package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.CartDAO;
import org.store.model.CartItem;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * Servlet implementation class CartServlet.
 * Manages the user's shopping cart via the endpoint {@code /api/cart}.
 * <p>
 * Supported operations:
 * <ul>
 * <li><b>GET:</b> Retrieve current cart items (requires {@code userId}).</li>
 * <li><b>POST:</b> Add an item to the cart (requires JSON body).</li>
 * <li><b>PUT:</b> Update item quantity (requires JSON body).</li>
 * <li><b>DELETE:</b> Remove an item (requires {@code userId} and {@code productId}).</li>
 * </ul>
 * </p>
 */
@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {
    private CartDAO cartDAO = new CartDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public CartServlet() {
        this.cartDAO = new CartDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param cartDAO Mock or real CartDAO.
     * @param gson Mock or real Gson.
     */
    public CartServlet(CartDAO cartDAO, Gson gson) {
        this.cartDAO = cartDAO;
        this.gson = gson;
    }


    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     *
     * @param resp The HTTP response object.
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
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
     * Handles GET requests to retrieve the shopping cart.
     * Expects a {@code userId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code userId}.
     * @param resp HttpServletResponse containing the list of cart items in JSON.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userId = req.getParameter("userId");
        if (userId != null) {
            List<CartItem> items = cartDAO.getCart(Long.parseLong(userId));
            resp.getWriter().write(gson.toJson(items));
        } else {
            resp.getWriter().write("[]");
        }
    }


    /**
     * Handles POST requests to add an item to the cart.
     * Reads JSON body containing {@code userId}, {@code productId}, and optional {@code quantity}.
     *
     * @param req  HttpServletRequest containing JSON payload.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        long userId = json.get("userId").getAsLong();
        long productId = json.get("productId").getAsLong();
        int quantity = json.has("quantity") ? json.get("quantity").getAsInt() : 1;

        if (cartDAO.addToCart(userId, productId, quantity)) {
            resp.getWriter().write("{\"message\": \"Added to cart\"}");
        } else {
            resp.setStatus(500);
        }
    }


    /**
     * Handles PUT requests to update item quantity.
     * Reads JSON body containing {@code userId}, {@code productId}, and new {@code quantity}.
     *
     * @param req  HttpServletRequest containing JSON payload.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        long userId = json.get("userId").getAsLong();
        long productId = json.get("productId").getAsLong();
        int quantity = json.get("quantity").getAsInt();

        if (cartDAO.updateQuantity(userId, productId, quantity)) {
            resp.getWriter().write("{\"message\": \"Quantity updated\"}");
        } else {
            resp.setStatus(500);
        }
    }


    /**
     * Handles DELETE requests to remove an item from the cart.
     * Expects {@code userId} and {@code productId} query parameters.
     *
     * @param req  HttpServletRequest containing query parameters.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        String userId = req.getParameter("userId");
        String productId = req.getParameter("productId");

        if (cartDAO.removeFromCart(Long.parseLong(userId), Long.parseLong(productId))) {
            resp.getWriter().write("{\"message\": \"Removed\"}");
        } else {
            resp.setStatus(500);
        }
    }
}