package com.sbank.netbanking.dbconfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class TestDbConnection {

    public static void main(String[] args) {
        Properties prop = new Properties();
        String filePath = "src/main/config/db.properties";

        try (InputStream inputStream = new FileInputStream(filePath)) {
            // Load the properties file
            prop.load(inputStream);

            
            String jdbcUrl = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            // Try to connect to the database
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Successfully connected to the database!");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading properties or connecting to database.");
            e.printStackTrace();
        }
    }
}