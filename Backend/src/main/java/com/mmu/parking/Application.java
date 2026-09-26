package com.mmu.parking;

import java.sql.Connection;
import java.sql.Statement;

public class Application {
    public static void main(String[] args) {
        // Log that we are checking our database setup on startup
        System.out.println("🔧 [AUTO-INIT] Verifying cloud database table schemas...");
        
        // SQL query to create the table that keeps track of the 100 parking spots
        String table1 = "CREATE TABLE IF NOT EXISTS parking_slots (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "slot_number VARCHAR(10) NOT NULL UNIQUE, " +
                        "status VARCHAR(20) DEFAULT 'VACANT')";
                        
        // SQL query to create the table that logs vehicle arrival, exit, and costs
        String table2 = "CREATE TABLE IF NOT EXISTS vehicle_logs (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "slot_id INT, " +
                        "license_plate VARCHAR(20) NOT NULL, " +
                        "entry_time DATETIME NOT NULL, " +
                        "exit_time DATETIME DEFAULT NULL, " +
                        "amount_paid DECIMAL(10, 2) DEFAULT 0.0)";

        // Connect to the database and run the table creation queries
        try (Connection conn = Database.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(table1);
                    stmt.execute(table2);
                    System.out.println("✅ [AUTO-INIT] Cloud database tables verified and ready!");
                }
            }
        } catch (Exception e) {
            // Print a warning message if the database table creation fails
            System.err.println("⚠️ [AUTO-INIT] Table setup warning: " + e.getMessage());
        }

        // Try to start up our core backend web server
        try {
            // Start the HTTP server listener setup for the project frontend
            ParkingController.startServer();
        } catch (Exception e) {
            // Error handling if port 8080 is blocked or server fails to boot
            System.out.println("❌ Failed to initialize offline server pipeline container.");
            e.printStackTrace();
        }
    }
}
