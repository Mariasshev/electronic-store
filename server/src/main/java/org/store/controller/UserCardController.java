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


/**
 * Servlet implementation class UserCardController.
 * Manages user payment methods via the endpoint {@code /api/cards}.
 * Supported operations:
 * <ul>
 * <li><b>GET:</b> Retrieve saved cards for a specific user.</li>
 * <li><b>POST:</b> Add a new card (Only stores the last 4 digits and brand).</li>
 * <li><b>DELETE:</b> Remove a saved card.</li>
 * </ul>
 */
@WebServlet("/api/cards")
public class UserCardController extends HttpServlet {
    private UserCardDAO cardDAO = new UserCardDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public UserCardController() {
        this.cardDAO = new UserCardDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param cardDAO Mock or real UserCardDAO.
     * @param gson Mock or real Gson.
     */
    public UserCardController(UserCardDAO cardDAO, Gson gson) {
        this.cardDAO = cardDAO;
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
        resp.setStatus(200);
    }

    /**
     * Handles GET requests to retrieve saved cards.
     * Expects a {@code userId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code userId}.
     * @param resp HttpServletResponse containing the list of cards in JSON.
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * Handles POST requests to save a new card.
     * <p>
     * Logic:
     * 1. Receives full card details (simulated, as real processing requires PCI DSS compliance).
     * 2. Determines the brand (Visa/Mastercard) based on the first digit.
     * 3. Extracts only the last 4 digits for storage.
     * </p>
     *
     * @param req  HttpServletRequest containing card JSON.
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

        // бренд
        String number = json.get("cardNumber").getAsString().replaceAll("\\s+", "");
        String brand = "MASTERCARD";
        if (number.startsWith("4")) brand = "VISA";

        // останні 4 цифри
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

    /**
     * Handles DELETE requests to remove a card.
     * Expects an {@code id} query parameter.
     *
     * @param req  HttpServletRequest containing the card ID.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
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