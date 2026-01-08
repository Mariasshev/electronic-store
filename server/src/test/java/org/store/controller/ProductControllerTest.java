package org.store.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.store.dao.ProductDAO;
import org.store.model.Product;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductController servlet;

    @Mock private ProductDAO productDAO;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new ProductController(productDAO, new Gson());
    }

    @Test
    void testGetAllProducts() throws Exception {
        Product mockProduct = new Product("Test Phone", new BigDecimal("500"), 5);
        mockProduct.setId(1L);

        when(productDAO.findAll()).thenReturn(List.of(mockProduct));

        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("categoryId")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        String output = stringWriter.toString();
        assertTrue(output.contains("Test Phone"));
        verify(productDAO).findAll();
    }

    @Test
    void testGetProductById_Found() throws Exception {
        Product mockProduct = new Product("Specific Phone", new BigDecimal("900"), 2);

        when(request.getParameter("id")).thenReturn("10");
        when(productDAO.findById(10L)).thenReturn(mockProduct);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        assertTrue(stringWriter.toString().contains("Specific Phone"));
        verify(productDAO).findById(10L);
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(request.getParameter("id")).thenReturn("999");
        when(productDAO.findById(999L)).thenReturn(null); // Товар не знайдено

        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doGet(request, response);

        verify(response).setStatus(404);
    }
}