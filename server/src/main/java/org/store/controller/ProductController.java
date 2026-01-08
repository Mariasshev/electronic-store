package org.store.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.dao.ProductDAO;
import org.store.model.Product;

import java.util.*;

/**
 * REST Controller for managing products.
 * Handles requests to '/api/products'.
 * <p>
 * Supported operations:
 * <ul>
 * <li>GET /api/products - Get all products or filter by criteria.</li>
 * <li>GET /api/products?id=1 - Get a single product by ID.</li>
 * <li>POST /api/products - Create a new product.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    private final ProductDAO productDAO;

    /**
     * Constructor Injection. Spring automatically provides the ProductDAO instance.
     */
    @Autowired
    public ProductController(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Handles GET requests to retrieve products.
     * Supports complex filtering logic inherited from the old Servlet.
     *
     * @param id         Optional product ID to fetch a single item.
     * @param categoryId Optional category ID for filtering.
     * @param brandId    Optional brand ID for filtering.
     * @param q          Optional search query string.
     * @param req        HttpServletRequest to extract dynamic 'spec_*' parameters.
     * @return List of products matching the criteria.
     */
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String q,
            HttpServletRequest req
    ) {
        if (id != null) {
            Product product = productDAO.findById(id);
            if (product != null) {
                return ResponseEntity.ok(product);
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        boolean hasComplexFilters = req.getParameterValues("brand") != null || hasSpecFilters(req);

        List<Product> products;

        if (hasComplexFilters && categoryId != null) {
            String[] brandParams = req.getParameterValues("brand");
            List<String> brands = brandParams != null ? Arrays.asList(brandParams) : new ArrayList<>();

            Map<String, List<String>> specs = extractSpecFilters(req);

            products = productDAO.findWithFilters(categoryId, brands, specs);

        } else if (categoryId != null || brandId != null || (q != null && !q.isEmpty())) {
            products = productDAO.findProducts(categoryId, brandId, q);

        } else {
            products = productDAO.findAll();
        }

        return ResponseEntity.ok(products);
    }

    /**
     * Handles POST requests to create a new product.
     * Spring automatically deserializes the JSON body into a Product object.
     *
     * @param product The product object from the request body.
     * @return Response with status and message.
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            if (productDAO.createProductFull(product)) {
                return ResponseEntity.ok(Map.of("message", "Product created successfully"));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Database error"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data format"));
        }
    }

    // --- Helper Methods for Parameter Extraction ---

    /**
     * Checks if the request contains any specification filters (starting with 'spec_').
     */
    private boolean hasSpecFilters(HttpServletRequest req) {
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            if (parameterNames.nextElement().startsWith("spec_")) return true;
        }
        return false;
    }

    /**
     * Extracts specification filters from request parameters into a Map.
     * Example: 'spec_Color=Red' -> Map{"Color": ["Red"]}
     */
    private Map<String, List<String>> extractSpecFilters(HttpServletRequest req) {
        Map<String, List<String>> specs = new HashMap<>();
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (paramName.startsWith("spec_")) {
                String key = paramName.substring(5);
                String[] values = req.getParameterValues(paramName);
                if (values != null) {
                    specs.put(key, Arrays.asList(values));
                }
            }
        }
        return specs;
    }
}