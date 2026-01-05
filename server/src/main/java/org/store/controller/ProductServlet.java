package org.store.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.store.dao.ProductDAO;
import org.store.model.Product;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/products") // Хороший тон: API эндпоинты начинать с /api
public class ProductServlet extends HttpServlet {

    private ProductDAO productDAO;
    private Gson gson;

    @Override
    public void init() {
        productDAO = new ProductDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Налаштування CORS
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");          // Для одного товара
        String categoryIdParam = req.getParameter("categoryId"); // НОВЫЙ ПАРАМЕТР

        String jsonResponse = "";

        try (PrintWriter out = resp.getWriter()) {
            if (idParam != null) {
                // Логика получения одного товара (как мы делали раньше)
                Long id = Long.parseLong(idParam);
                Product product = productDAO.findById(id);
                jsonResponse = gson.toJson(product);
            } else if (categoryIdParam != null) {
                // НОВАЯ ЛОГИКА: Фильтр по категории
                Long catId = Long.parseLong(categoryIdParam);
                List<Product> products = productDAO.findByCategoryId(catId);
                jsonResponse = gson.toJson(products);
            } else {
                // Если параметров нет — возвращаем ВСЕ товары
                List<Product> products = productDAO.findAll();
                jsonResponse = gson.toJson(products);
            }
            out.print(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
        }


        try (PrintWriter out = resp.getWriter()) {
            if (idParam != null && !idParam.isEmpty()) {
                // ВАРІАНТ 1: Якщо є ID — шукаємо один товар
                Long id = Long.parseLong(idParam);
                Product product = productDAO.findById(id);

                if (product != null) {
                    jsonResponse = gson.toJson(product); // <--- Повертаємо ОБ'ЄКТ
                } else {
                    resp.setStatus(404);
                    jsonResponse = "{}";
                }
            } else {
                // ВАРІАНТ 2: Якщо ID немає — повертаємо список
                List<Product> products = productDAO.findAll();
                jsonResponse = gson.toJson(products); // <--- Повертаємо МАСИВ
            }
            out.print(jsonResponse);
        } catch (Exception e) {
            resp.setStatus(500);
            e.printStackTrace();
        }
    }
}