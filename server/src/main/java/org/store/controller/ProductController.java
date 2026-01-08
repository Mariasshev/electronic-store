package org.store.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.store.dao.ProductDAO;
import org.store.model.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class ProductController.
 * Handles HTTP requests for Products (/api/products).
 * Supports GET (fetching products) and POST (creating products).
 */
@WebServlet("/api/products")
public class ProductController extends HttpServlet {

    private ProductDAO productDAO;
    private Gson gson;

    /**
     * Default constructor for Tomcat container.
     * Initializes DAO and Gson with default implementations.
     */
    public ProductController() {
        this.productDAO = new ProductDAO();
        this.gson = new Gson();
    }

    /**
     * Constructor for Unit Testing (Dependency Injection).
     * @param productDAO Mock or real DAO.
     * @param gson Mock or real Gson.
     */
    public ProductController(ProductDAO productDAO, Gson gson) {
        this.productDAO = productDAO;
        this.gson = gson;
    }


    /**
     * Sets Cross-Origin Resource Sharing (CORS) headers to allow frontend communication.
     * Configures the response to accept requests from the React application running on port 3000.
     * Headers set:
     * <ul>
     * <li>Access-Control-Allow-Origin: allows localhost:3000</li>
     * <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS</li>
     * <li>Access-Control-Allow-Headers: Content-Type, Authorization</li>
     * <li>Access-Control-Allow-Credentials: allows cookies/auth headers</li>
     * </ul>
     *
     * @param resp The HttpServletResponse object to modify.
     */
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


    /**
     * Handles HTTP OPTIONS requests (CORS Preflight).
     * Browsers send an OPTIONS request before a POST/PUT request to check permissions.
     * This method simply sets the CORS headers and returns status 200 OK.
     *
     * @param req  The HTTP request.
     * @param resp The HTTP response.
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(200);
    }


    /**
     * Handles HTTP POST requests to create a new product.
     * <p>
     * Workflow:
     * 1. Reads the raw JSON body from the request.
     * 2. Deserializes JSON into a {@link Product} object using Gson.
     * 3. Calls DAO to save the product to the database.
     * </p>
     *
     * Responses:
     * <ul>
     * <li>200 OK: Product created successfully.</li>
     * <li>400 Bad Request: Invalid JSON format.</li>
     * <li>500 Internal Server Error: Database operation failed.</li>
     * </ul>
     *
     * @param req  The HTTP request containing the JSON payload.
     * @param resp The HTTP response.
     * @throws IOException If an input or output error occurs.
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


    /**
     * Handles GET requests.
     * Supports parameters:
     * - id: fetch single product
     * - q: search by name
     * - categoryId: filter by category (supports 'brand' and 'spec_*' filters)
     * - none: fetch all products
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String idParam = req.getParameter("id");
        String searchParam = req.getParameter("q");
        String categoryParam = req.getParameter("categoryId");

        System.out.println("--- ProductController GET Request ---");
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