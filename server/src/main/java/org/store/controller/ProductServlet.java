package org.store.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.store.dao.ProductDAO;
import org.store.model.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/products")
public class ProductServlet extends HttpServlet {

    private ProductDAO productDAO;
    private Gson gson;

    public ProductServlet() {
        this.productDAO = new ProductDAO();
        this.gson = new Gson();
    }

    // 2. Конструктор З параметрами (Для ТЕСТІВ)
    // Ми будемо викликати його в JUnit і передавати туди Mock-об'єкти.
    public ProductServlet(ProductDAO productDAO, Gson gson) {
        this.productDAO = productDAO;
        this.gson = gson;
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        // Дозволяємо запити з React-фронтенду
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        // Дозволяємо методи
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // Дозволяємо заголовки
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Дозволяємо передачу кукі/авторизації
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        try {
            Product newProduct = gson.fromJson(sb.toString(), Product.class);

            if (productDAO.createProductFull(newProduct)) {
                resp.getWriter().write("{\"message\": \"Product created successfully\"}");
            } else {
                resp.setStatus(500);
                resp.getWriter().write("{\"error\": \"Database error\"}");
            }
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String idParam = req.getParameter("id");
        String searchParam = req.getParameter("q");
        String categoryParam = req.getParameter("categoryId");

        System.out.println("--- ProductServlet GET Request ---");
        System.out.println("Query String: " + req.getQueryString());

        // 1. Пошук
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            List<Product> products = productDAO.searchByName(searchParam);
            resp.getWriter().write(gson.toJson(products));
            return;
        }

        // 2. Один товар
        if (idParam != null) {
            Product product = productDAO.findById(Long.parseLong(idParam));
            if (product != null) {
                resp.getWriter().write(gson.toJson(product));
            } else {
                resp.setStatus(404);
                resp.getWriter().write("{}");
            }
            return;
        }

        // 3. Категорія + ФІЛЬТРИ (Catalog Page)
        if (categoryParam != null) {
            long catId = Long.parseLong(categoryParam);

            // Зчитуємо бренди
            String[] brandParams = req.getParameterValues("brand");
            List<String> brands = brandParams != null ? java.util.Arrays.asList(brandParams) : new ArrayList<>();
            System.out.println("Selected Brands: " + brands);

            // Зчитуємо характеристики
            Map<String, List<String>> specs = new java.util.HashMap<>();
            java.util.Enumeration<String> parameterNames = req.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                if (paramName.startsWith("spec_")) {
                    String key = paramName.substring(5);
                    String[] values = req.getParameterValues(paramName);
                    if (values != null) {
                        specs.put(key, java.util.Arrays.asList(values));
                    }
                }
            }
            System.out.println("Selected Specs: " + specs);

            // Викликаємо метод з фільтрами
            List<Product> products = productDAO.findWithFilters(catId, brands, specs);
            System.out.println("Found products: " + products.size());

            resp.getWriter().write(gson.toJson(products));
            return;
        }

        // 4. Інакше - повертаємо всі товари (якщо нічого не вибрано)
        List<Product> products = productDAO.findAll();
        resp.getWriter().write(gson.toJson(products));
    }
}