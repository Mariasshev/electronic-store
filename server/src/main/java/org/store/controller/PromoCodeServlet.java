package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.PromoCodeDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet implementation class PromoCodeServlet.
 * Handles promotional code validation via the endpoint {@code /api/promo}.
 * Supported operations:
 * <ul>
 * <li><b>POST:</b> Validate a promo code. Expects JSON: {@code {"code": "SUMMER2024"}}</li>
 * <li><b>OPTIONS:</b> Handle CORS preflight requests.</li>
 * </ul>
 */
@WebServlet("/api/promo")
public class PromoCodeServlet extends HttpServlet {
    private PromoCodeDAO promoDAO = new PromoCodeDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public PromoCodeServlet() {
        this.promoDAO = new PromoCodeDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param promoDAO Mock or real PromoCodeDAO.
     * @param gson Mock or real Gson.
     */
    public PromoCodeServlet(PromoCodeDAO promoDAO, Gson gson) {
        this.promoDAO = promoDAO;
        this.gson = gson;
    }

    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     *
     * @param resp The HTTP response object.
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
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
     * Handles POST requests to validate a promo code.
     * <p>
     * Request Body Example: {@code {"code": "DISCOUNT10"}}<br>
     * Response Example (Success): {@code {"valid": true, "discountPercent": 10}}<br>
     * Response Example (Failure): {@code {"valid": false}}
     * </p>
     *
     * @param req  HttpServletRequest containing the JSON body.
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