package org.store.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521/FREE";

    public static Connection getConnection() {
        try {
            Properties props = new Properties();
            props.put("user", "SYS");
            props.put("password", "1");
            props.put("internal_logon", "SYSDBA");

            return DriverManager.getConnection(URL, props);

        } catch (SQLException e) {
            System.out.println("Failed to connect to Oracle DB!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
