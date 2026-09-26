package com.mmu.parking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    
    // Divided into two explicit segments to guarantee no text truncation happens
    private static final String PART1 = "jdbc:mysql://mysql-5fc95bd-mastersovietusa-f670.e.aivencloud.com";
    private static final String PART2 = ":26148/defaultdb?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    
    private static final String URL = PART1 + PART2;
    private static final String USER = "avnadmin"; 
    private static final String PASSWORD = "AVNS_s5yTsBQR2cUl2kcc5lB"; 
    
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println("❌ Remote Aiven Cloud Database Connection Error!");
            e.printStackTrace();
        }
        return connection;             
    }
}
