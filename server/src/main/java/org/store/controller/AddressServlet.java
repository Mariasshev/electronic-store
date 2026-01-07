package org.store.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.store.dao.AddressDAO;
import org.store.model.Address;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;


/**
 * Servlet implementation class AddressServlet.
 * Manages user shipping addresses via the endpoint {@code /api/addresses}.
 * <p>
 * Supported operations:
 * <ul>
 * <li><b>GET:</b> Retrieve all addresses for a specific user (requires {@code userId} parameter).</li>
 * <li><b>POST:</b> Add a new address (requires JSON body).</li>
 * <li><b>DELETE:</b> Remove an address (requires {@code id} parameter).</li>
 * <li><b>OPTIONS:</b> Handle CORS preflight requests.</li>
 * </ul>
 * </p>
 */
@WebServlet("/api/addresses/*")
public class AddressServlet extends HttpServlet {

    private AddressDAO addressDAO = new AddressDAO();
    private Gson gson = new Gson();

    /**
     * Default constructor for the Servlet container (Tomcat).
     * Initializes dependencies with real implementations.
     */
    public AddressServlet() {
        this.addressDAO = new AddressDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing.
     * Allows injection of Mock objects.
     *
     * @param addressDAO Mock or real AddressDAO.
     * @param gson Mock or real Gson.
     */
    public AddressServlet(AddressDAO addressDAO, Gson gson) {
        this.addressDAO = addressDAO;
        this.gson = gson;
    }


    /**
     * Sets standard Cross-Origin Resource Sharing (CORS) headers.
     * Allows requests from {@code http://localhost:3000} (React Frontend).
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
     * Handles GET requests to retrieve a list of addresses.
     * Expects a {@code userId} query parameter.
     *
     * @param req  HttpServletRequest containing {@code userId}.
     * @param resp HttpServletResponse containing the list of addresses in JSON format.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String userIdParam = req.getParameter("userId");
        if (userIdParam != null) {
            Long userId = Long.parseLong(userIdParam);
            List<Address> addresses = addressDAO.findByUserId(userId);
            resp.getWriter().write(gson.toJson(addresses));
        } else {
            resp.getWriter().write("[]");
        }
    }

    /**
     * Handles POST requests to add a new address.
     * Reads the address details from the JSON body.
     *
     * @param req  HttpServletRequest containing the Address JSON payload.
     * @param resp HttpServletResponse indicating success or failure.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        Address newAddr = gson.fromJson(sb.toString(), Address.class);
        if (addressDAO.addAddress(newAddr)) {
            resp.getWriter().write("{\"status\":\"ok\"}");
        } else {
            resp.setStatus(500);
        }
    }

    /**
     * Handles DELETE requests to remove a specific address.
     * Expects an {@code id} query parameter identifying the address to delete.
     *
     * @param req  HttpServletRequest containing {@code id}.
     * @param resp HttpServletResponse.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        String idParam = req.getParameter("id");
        if (idParam != null) {
            Long id = Long.parseLong(idParam);
            addressDAO.deleteAddress(id);
            resp.setStatus(200);
        }
    }
}