package com.mmu.parking;

import java.time.LocalDateTime;

// Class that represents a vehicle parked inside the system
public class Vehicle {
    private String licensePlate;
    private String slotAssigned;
    private LocalDateTime entryTime;
    
    // Constructor used when a new car checks into the parking lot
    public Vehicle(String licensePlate, String slotAssigned) {
        this.licensePlate = licensePlate;
        this.slotAssigned = slotAssigned;
        this.entryTime = LocalDateTime.now(); // Saves the current date and time as the entry mark
    }

    // Overloaded constructor used to restore active cars from the database on system reboot
    public Vehicle(String licensePlate, String slotAssigned, LocalDateTime entryTime) {
        this.licensePlate = licensePlate;
        this.slotAssigned = slotAssigned;
        this.entryTime = entryTime; // Restores the original entry time saved in the database
    }

    // Getter to retrieve the car's plate number
    public String getlicensePlate() {
        return licensePlate;
    }

    // Getter to retrieve the exact timestamp when the vehicle entered
    public LocalDateTime getentryTime() {
        return entryTime;
    }

    // Getter to check which slot number was assigned to this vehicle
    public String getslotAssigned() {
        return slotAssigned;
    }
}
