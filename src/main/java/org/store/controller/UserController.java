package org.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.store.dao.UserDAO;
import org.store.model.User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    private final UserDAO userDAO = new UserDAO();

    @GetMapping("/users")
    public String usersPage(Model model) {
        List<User> users = userDAO.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
}
