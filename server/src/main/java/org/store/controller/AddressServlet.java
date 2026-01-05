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

@WebServlet("/api/addresses/*")
public class AddressServlet extends HttpServlet {

    private AddressDAO addressDAO = new AddressDAO();
    private Gson gson = new Gson();

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    // GET: Отримати список адрес (передаємо ?userId=...)
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

    // POST: Додати адресу
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

    // DELETE: Видалити адресу (передаємо ?id=...)
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