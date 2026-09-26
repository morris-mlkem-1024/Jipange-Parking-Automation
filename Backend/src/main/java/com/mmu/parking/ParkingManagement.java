package com.mmu.parking;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Core class to manage parking slots and handle vehicle check-in/check-out
public class ParkingManagement {
    
    // Lists to hold all 100 parking spots
    private ArrayList<Slot> layout = new ArrayList<>();
    
    // Map to keep track of active vehicles currently inside the facility
    private HashMap<String, Vehicle> activeVehicle = new HashMap<>();

    // Constructor to initialize slots and automatically load cars from the cloud database on startup
    public ParkingManagement(int totalSlots) {
        // Initialize the default layout structure
        for (int i = 1; i <= totalSlots; i++) {
            layout.add(new Slot(i, "A" + i));
        }

        // Auto-load uncleared vehicles from the database to keep data consistent
        System.out.println("Loading active vehicle records from cloud database...");
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
                        
                        // Safety check to ensure records fit within our 100 slots range
                        if (slotId >= 1 && slotId <= layout.size()) {
                            Slot slot = layout.get(slotId - 1);
                            slot.setStatus("OCCUPIED"); 
                            
                            LocalDateTime originalEntryTime = entryTimestamp.toLocalDateTime();
                            Vehicle vehicle = new Vehicle(licensePlate, slot.getSlotNumber(), originalEntryTime);
                            
                            activeVehicle.put(licensePlate, vehicle);
                            recoveredCount++;
                        }
                    }
                    System.out.println("Data recovery complete. Restored " + recoveredCount + " active slots.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Warning: Data recovery loop encountered an exception.");
            e.printStackTrace();
        }
    }

    // Calculates the parking fees based on total duration spent inside
    public double fee(String licensePlate) {
        Vehicle vehicle = activeVehicle.get(licensePlate);
        if (vehicle == null) {       
            System.err.println("Warning: Request received for a vehicle not in memory.");
            return 0.0;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long totalDuration = ChronoUnit.MINUTES.between(vehicle.getentryTime(), exitTime);
        double amountPayable = 0.0;

        // Simple tiered pricing logic
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

    // Handles incoming vehicles, assigns an empty space, and saves logs to the database
    public String checkIn(String licensePlate) {
        if (activeVehicle.containsKey(licensePlate)) {
            return "ERROR: A vehicle with license plate " + licensePlate + " is already checked in.";
        }

        Slot slotAssigned = null;
        for (Slot slot : layout) {
            if (slot.getStatus().equalsIgnoreCase("VACANT")) {
                slotAssigned = slot;
                break;
            }
        }
        
        if (slotAssigned == null) {
            return "REGRET: The parking facility is currently full.";
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
            System.err.println("Database rollback executed during check-in failure.");
            e.printStackTrace();
            return "SYSTEM WARNING: Vehicle tracked in cache but failed database sync.";
        }

        return "Welcome! Vehicle " + licensePlate + " successfully assigned to slot: " + slotAssigned.getSlotNumber(); 
    }

    // Processes exits, checks fees, simulates M-Pesa push, and updates database records
    public String checkOut(String licensePlate) {
        Vehicle vehicle = activeVehicle.get(licensePlate);
        if (vehicle == null) {
            return "ERROR: License plate " + licensePlate + " is not registered in active tracking tables.";
        }
        
        double finalAmount = fee(licensePlate);

        System.out.println("Simulating M-Pesa STK Push authorization...");
        System.out.println("Requesting KES " + finalAmount + " from customer mobile device...");
        
        boolean paymentAuthorizedSuccessfully = true; 

        if (!paymentAuthorizedSuccessfully) {
            return "ERROR: STK Push declined or transaction timed out.";
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
                    logStmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    logStmt.executeUpdate();
                }
                
                try (PreparedStatement slotStmt = conn.prepareStatement(updateSlotSQL)) {
                    slotStmt.setString(1, vehicle.getslotAssigned());
                    slotStmt.executeUpdate();
                }
                
                conn.commit();
            }
        } catch (SQLException e) {
            System.err.println("Database sync error encountered during exit operations.");
            e.printStackTrace();
            return "CHECKOUT FAILED: Database log synchronization error.";
        }
        
        return "M-Pesa Payment of KES " + finalAmount + " Confirmed! \nBarrier Opening Automatically. Drive Safely!";
    }

    // Returns the collection of spaces
    public ArrayList<Slot> getLayout() {
        return layout;
    }
}
