package com.mmu.parking;

import java.sql.Connection;
import java.sql.Statement;

public class Application {
    public static void main(String[] args) {
        System.out.println("🔧 [AUTO-INIT] Verifying cloud database table schemas...");
        
        String table1 = "CREATE TABLE IF NOT EXISTS parking_slots (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "slot_number VARCHAR(10) NOT NULL UNIQUE, " +
                        "status VARCHAR(20) DEFAULT 'VACANT')";
                        
        String table2 = "CREATE TABLE IF NOT EXISTS vehicle_logs (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "slot_id INT, " +
                        "license_plate VARCHAR(20) NOT NULL, " +
                        "entry_time DATETIME NOT NULL, " +
                        "exit_time DATETIME DEFAULT NULL, " +
                        "amount_paid DECIMAL(10, 2) DEFAULT 0.0)";

        try (Connection conn = Database.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(table1);
                    stmt.execute(table2);
                    System.out.println("✅ [AUTO-INIT] Cloud database tables verified and ready!");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [AUTO-INIT] Table setup warning: " + e.getMessage());
        }

        try {
            // Ignites your custom native web framework wrapper completely offline
            ParkingController.startServer();
        } catch (Exception e) {
            System.out.println("❌ Failed to initialize offline server pipeline container.");
            e.printStackTrace();
        }
    }
}
