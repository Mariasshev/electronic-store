package org.store.dao;

import org.store.model.User;
import java.util.List;

public interface UserDAO {
    void addUser(User user);
    User getUserByEmail(String email);
    List<User> getAllUsers();
}