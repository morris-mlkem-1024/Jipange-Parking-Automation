package com.mmu.parking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    
    // Breaking the connection link into two pieces so that it doesn't get cut off in the terminal. We combine below
    private static final String PART1 = "jdbc:mysql://mysql-5fc95bd-mastersovietusa-f670.e.aivencloud.com";
    private static final String PART2 = ":26148/defaultdb?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    
    // Combine the strings together to make the full database path link
    private static final String URL = PART1 + PART2;
    private static final String USER = "avnadmin"; 
    private static final String PASSWORD = "AVNS_s5yTsBQR2cUl2kcc5lB"; 
    
    // Method to create and return the active database connection string
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC connector driver class into the runtime path
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to our remote cloud database using our credentials
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            // Error handling block if the remote server rejects the connection request
            System.err.println("❌ Remote Aiven Cloud Database Connection Error!");
            e.printStackTrace();
        }
        return connection;             
    }
}
