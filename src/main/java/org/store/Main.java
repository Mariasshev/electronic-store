package org.store;

import org.store.dao.UserDAO;
import org.store.model.User;

public class Main {
    public static void main(String[] args) {

        UserDAO dao = new UserDAO();

        System.out.println("=== CREATE ===");
        dao.addUser(new User("Maria", "maria@test.com", "12345", "admin"));

        System.out.println("=== READ ===");
        dao.getAllUsers().forEach(System.out::println);

        System.out.println("=== UPDATE ===");
        dao.updateUserName(1, "Maria Updated");

        System.out.println("=== DELETE ===");
        dao.deleteUser(1);
    }
}
