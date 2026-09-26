package com.mmu.parking;

import java.time.LocalDateTime;

/**
 * ARCHITECTURAL LAYER: Entity Data Model
 * PURPOSE: Represents active vehicular nodes stored within the system's memory runtime cache.
 * It locks in immutable arrival stamps to secure tracking data arrays against back-dated 
 * or falsified checkout time requests.
 */
public class Vehicle {
    private String licensePlate;
    private String slotAssigned;
    private LocalDateTime entryTime;
    
    /**
     * Constructor launched during the entry processing handshake pipeline.
     * Snaps system clock metrics to preserve chronological integrity.
     * 
     * @param licensePlate Unique registration character marker string.
     * @param slotAssigned Designated terminal zone coordinates code.
     */
    public Vehicle(String licensePlate, String slotAssigned) {
        this.licensePlate = licensePlate;
        this.slotAssigned = slotAssigned;
        this.entryTime = LocalDateTime.now(); // Snapshots transaction timestamp records immediately
    }

    /**
     * Overloaded constructor triggered during system rehydration phases.
     * Restores historical chronological markers precisely out of persistent SQL row records.
     */
    public Vehicle(String licensePlate, String slotAssigned, LocalDateTime entryTime) {
        this.licensePlate = licensePlate;
        this.slotAssigned = slotAssigned;
        this.entryTime = entryTime; // Restores the exact original database clock marker
    }

    /**
     * Extracts the unique license identifier value.
     */
    public String getlicensePlate() {
        return licensePlate;
    }

    /**
     * Pulls the absolute epoch date-time marker denoting facility entrance.
     */
    public LocalDateTime getentryTime() {
        return entryTime;
    }

    /**
     * Identifies the current grid allocation zone string mapping.
     */
    public String getslotAssigned() {
        return slotAssigned;
    }
}
