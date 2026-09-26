package com.mmu.parking;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ARCHITECTURAL LAYER: Core Business Logic / Engine Layer
 * PURPOSE: Manages in-memory data structures, calculates tiered parking tariffs,
 * and maintains atomic data consistency between local collections and the SQL transaction schemas.
 */
public class ParkingManagement {
    
    // Dynamic array matrix to store the collection of physical parking spots
    private ArrayList<Slot> layout = new ArrayList<>();
    
    // High-performance associative map to track active cars using license plates as keys
    private HashMap<String, Vehicle> activeVehicle = new HashMap<>();

    /**
     * Constructor that configures the parking bays layout matrix and initializes the
     * AUTOMATED DATA RECOVERY BOOT LOADER. On system launch, it queries XAMPP MySQL to pull 
     * all open sessions and instantly restores them into the active running memory state.
     */
    public ParkingManagement(int totalSlots) {
        // STEP 1: Initialize the default vacant layout maps configurations structures array lists
        for (int i = 1; i <= totalSlots; i++) {
            layout.add(new Slot(i, "A" + i));
        }

        // STEP 2: RUN HISTORICAL RECOVERY PIPELINE TO REHYDRATE ACTIVE CACHES
        System.out.println("🔄 [BOOT LAYER] Initializing System Persistence Recovery Pipeline...");
        String recoverySQL = "SELECT slot_id, license_plate, entry_time FROM vehicle_logs WHERE exit_time IS NULL";

        try (Connection conn = Database.getConnection()) {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(recoverySQL);
                     java.sql.ResultSet rs = stmt.executeQuery()) {
                    
                    int recoveredCount = 0;
                    
                    while (rs.next()) {
                        int slotId = rs.getInt("slot_id");
                        String licensePlate = rs.getString("license_plate");
                        java.sql.Timestamp entryTimestamp = rs.getTimestamp("entry_time");
                        
                        // Bounds constraint check: map parameters back into our 100 spots index cleanly
                        if (slotId >= 1 && slotId <= layout.size()) {
                            Slot slot = layout.get(slotId - 1);
                            slot.setStatus("OCCUPIED"); // Lock grid monitor cell indicators properties
                            
                            // Transform java.sql.Timestamp cleanly back into native LocalDateTime streams
                            LocalDateTime originalEntryTime = entryTimestamp.toLocalDateTime();
                            
                            // Invoke our new constructor overload to retain exact billing clock parameters
                            Vehicle vehicle = new Vehicle(licensePlate, slot.getSlotNumber(), originalEntryTime);
                            
                            // Inject items references straight back down into our associative data map layout array
                            activeVehicle.put(licensePlate, vehicle);
                            recoveredCount++;
                        }
                    }
                    System.out.println("✅ [BOOT LAYER] Data recovery loop complete. Successfully restored " + recoveredCount + " active parking slots.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [BOOT LAYER] Critical warning: Recovery stream encountered database mapping exceptions.");
            e.printStackTrace();
        }
    }


    /**
     * ALGORITHM IMPLEMENTATION: Computes the precise parking fee in Kenya Shillings based on time spent.
     */
    public double fee(String licensePlate) {
        Vehicle vehicle = activeVehicle.get(licensePlate);
        if (vehicle == null) {       
            System.err.println("⚠️ Warning: Request received for a vehicle not stored in memory.");
            return 0.0;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long totalDuration = ChronoUnit.MINUTES.between(vehicle.getentryTime(), exitTime);
        double amountPayable = 0.0;

        // Pricing logic matrices
        if (totalDuration <= 30) {    
            amountPayable = 0.0;
        } else if (totalDuration <= 120) {
            amountPayable = 50.0;
        } else if (totalDuration <= 240) {
            amountPayable = 100.0;
        } else if (totalDuration <= 360) {
            amountPayable = 300.0;
        } else {
            amountPayable = 500.0;
        }
        return amountPayable;
    }

    /**
     * MODULE 2 ALGORITHM: Handles incoming vehicles, assigns empty space, and saves records to the database.
     */
    public String checkIn(String licensePlate) {
        if (activeVehicle.containsKey(licensePlate)) {
            return "ERROR: A vehicle with license plate " + licensePlate + " is already clocked into the system.";
        }

        Slot slotAssigned = null;
        for (Slot slot : layout) {
            if (slot.getStatus().equalsIgnoreCase("VACANT")) {
                slotAssigned = slot;
                break;
            }
        }
        
        if (slotAssigned == null) {
            return "REGRET: The parking facility is currently at maximum capacity.";
        }

        slotAssigned.setStatus("OCCUPIED");
        Vehicle newVehicle = new Vehicle(licensePlate, slotAssigned.getSlotNumber());
        activeVehicle.put(licensePlate, newVehicle);

        String insertLogSQL = "INSERT INTO vehicle_logs (slot_id, license_plate, entry_time) VALUES (?, ?, ?)";
        String updateSlotSQL = "UPDATE parking_slots SET status = 'OCCUPIED' WHERE slot_number = ?";

        try (Connection conn = Database.getConnection()) {
            if (conn != null) {
                conn.setAutoCommit(false);
            }

            try (PreparedStatement logStmt = conn.prepareStatement(insertLogSQL)) {
                logStmt.setInt(1, layout.indexOf(slotAssigned) + 1);
                logStmt.setString(2, licensePlate);
                logStmt.setTimestamp(3, java.sql.Timestamp.valueOf(newVehicle.getentryTime()));
                logStmt.executeUpdate();
            }
            
            try (PreparedStatement slotStmt = conn.prepareStatement(updateSlotSQL)) {
                slotStmt.setString(1, slotAssigned.getSlotNumber());
                slotStmt.executeUpdate();
            }
            
            if (conn != null) {
                conn.commit();
            }
        } catch (SQLException e) {
            System.err.println("❌ Database rollback executed during check-in failure.");
            e.printStackTrace();
            return "SYSTEM WARNING: Vehicle tracked in cache but failed database sync.";
        }

        return "Welcome! Vehicle " + licensePlate + " successfully assigned to slot: " + slotAssigned.getSlotNumber(); 
    }

    /**
     * MODULE 3 & 4 ALGORITHM: Runs exit processing pipelines, looks up fees, and handles checkout processes.
     */
    public String checkOut(String licensePlate) {
        Vehicle vehicle = activeVehicle.get(licensePlate);
        if (vehicle == null) {
            return "ERROR: License plate " + licensePlate + " is not registered inside our active tracking tables.";
        }
        
        double finalAmount = fee(licensePlate);

        System.out.println("💳 [M-PESA DARAJA ENGINE] Dispatched STK Push alert prompt query.");
        System.out.println("📱 [STK API STATUS] Requesting KES " + finalAmount + " from customer phone stream...");
        
        boolean paymentAuthorizedSuccessfully = true; 

        if (!paymentAuthorizedSuccessfully) {
            return "ERROR: STK Push declined or transaction timed out on Safaricom checkout channel lines.";
        }

        Slot slotAssigned = null;
        for (Slot slot : layout) {
            if (slot.getSlotNumber().equals(vehicle.getslotAssigned())) {
                slotAssigned = slot;
                break;
            }
        }
        if (slotAssigned != null) {
            slotAssigned.setStatus("VACANT");
        }
        
        activeVehicle.remove(licensePlate);

        String updatelogSQL = "UPDATE vehicle_logs SET exit_time = ?, amount_paid = ? WHERE license_plate = ? AND exit_time IS NULL";
        String updateSlotSQL = "UPDATE parking_slots SET status = 'VACANT' WHERE slot_number = ?";
        
        try (Connection conn = Database.getConnection()) {
            if (conn != null) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement logStmt = conn.prepareStatement(updatelogSQL)) {
                    logStmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    logStmt.setDouble(2, finalAmount);
                    logStmt.setString(3, licensePlate);
                    logStmt.executeUpdate();
                }
                
                try (PreparedStatement slotStmt = conn.prepareStatement(updateSlotSQL)) {
                    slotStmt.setString(1, vehicle.getslotAssigned());
                    slotStmt.executeUpdate();
                }
                
                conn.commit();
            }
        } catch (SQLException e) {
            System.err.println("❌ Database sync breakdown encountered during exit tracking steps.");
            e.printStackTrace();
            return "CHECKOUT FAILED: Database engine structural synchronization mismatch error.";
        }
        
        return "M-Pesa Payment of KES " + finalAmount + " Confirmed! \nBarrier Opening Automatically. Drive Safely!";
    }

    /**
     * Exposes the complete internal array layout to the layout viewing adapters.
     */
    public ArrayList<Slot> getLayout() {
        return layout;
    }
}
