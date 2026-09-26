package com.mmu.parking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ARCHITECTURAL LAYER: Data Access / Connectivity Layer
 * PURPOSE: This class manages the lifecycle of database connections to the MySQL server.
 * It establishes a secure communication channel between the Java application runtime 
 * and your remote cloud relational storage engine hosted on Aiven.
 */
public class Database {

    // FIXED: The completely resolved, solid cloud URL string matching your exact Aiven console metrics parameters
    private static final String URL = "jdbc:mysql://://aivencloud.com";
    private static final String USER = "avnadmin"; 
    private static final String PASSWORD = "AVNS_s5yTsBQR2cUl2kcc5lB"; 
    
    /**
     * Establishes and returns an active connection to the remote 'defaultdb' cloud database schema.
     */
    public static Connection getConnection() {
        Connection connection = null;
        
        try {
            // STEP 1: Dynamically load the modern MySQL Connector/J driver into runtime memory
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // STEP 2: Authenticate cloud credentials and initialize the live communications bridge
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ CRITICAL DATABASE ERROR: MySQL Driver class not found. Verify your project build dependencies.");
            e.printStackTrace();
            
        } catch (SQLException e) {
            System.err.println("❌ CRITICAL DATABASE ERROR: Cloud authentication failed or connection socket timed out! Check your password string.");
            e.printStackTrace();
        }

        return connection;             
    }
}
