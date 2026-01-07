package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.OrderDAO;
import org.store.model.Order;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;


/**
 * Servlet implementation class OrderServlet.
 * Manages order processing via the endpoint {@code /api/orders}.
 * Supported operations:
 * <ul>
 * <li><b>GET:</b> Retrieve order history for a specific user (requires {@code userId}).</li>
 * <li><b>POST:</b> Create a new order (checkout) based on the user's cart.</li>
 * <li><b>OPTIONS:</b> Handle CORS preflight requests.</li>
 * </ul>
 */
@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public OrderServlet() {
        this.orderDAO = new OrderDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param orderDAO Mock or real OrderDAO.
     * @param gson Mock or real Gson.
     */
    public OrderServlet(OrderDAO orderDAO, Gson gson) {
        this.orderDAO = orderDAO;
        this.gson = gson;
    }

    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     *
     * @param resp The HTTP response object.
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
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
     * Handles GET requests to retrieve order history.
     * Expects a {@code userId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code userId}.
     * @param resp HttpServletResponse containing the list of orders in JSON.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userId = req.getParameter("userId");
        if (userId != null) {
            List<Order> orders = orderDAO.getUserOrders(Long.parseLong(userId));
            resp.getWriter().write(gson.toJson(orders));
        }
    }

    /**
     * Handles POST requests to create a new order (Checkout).
     * Requires a JSON body with {@code userId}, {@code address}, {@code phone}, and {@code total}.
     *
     * @param req  HttpServletRequest containing order details.
     * @param resp HttpServletResponse indicating success or failure.
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
        String address = json.get("address").getAsString();
        String phone = json.get("phone").getAsString();
        BigDecimal total = json.get("total").getAsBigDecimal();

        if (orderDAO.createOrder(userId, address, phone, total)) {
            resp.getWriter().write("{\"message\": \"Order created\"}");
        } else {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"Order failed\"}");
        }
    }
}