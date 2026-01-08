package org.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.store.dao.ProductDAO;
import org.store.dto.FilterDTO;

@RestController
@RequestMapping("/api/filters")
@CrossOrigin(origins = "http://localhost:3000")
public class FilterController {

    private final ProductDAO productDAO;

    @Autowired
    public FilterController(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * GET: Retrieve available filters for a category.
     * URL: /api/filters?categoryId=1
     */
    @GetMapping
    public FilterDTO getFilters(@RequestParam Long categoryId) {
        return productDAO.getFiltersByCategory(categoryId);
    }
}