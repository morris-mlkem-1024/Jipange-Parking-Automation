package com.mmu.parking;

// Class that represents a single physical parking slot in the system
public class Slot {
    private int slotId;
    private String slotNumber;
    private String status;

    // Constructor to set up a parking spot. All spots start as vacant by default.
    public Slot(int slotId, String slotNumber) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.status = "VACANT"; // Set status to vacant on system setup
    }

    // Getter to retrieve the slot name/number (like A1, A2) for the frontend
    public String getSlotNumber() {
        return slotNumber;
    }

    // Getter to check if the slot is currently VACANT or OCCUPIED
    public String getStatus() {
        return status;
    }

    // Setter to update the slot status when a car enters or exits the space
    public void setStatus(String status) {
        this.status = status;
    }
}
