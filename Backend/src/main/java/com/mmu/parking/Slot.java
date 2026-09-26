package com.mmu.parking;

/**
 * ARCHITECTURAL LAYER: Entity Data Model
 * PURPOSE: Maps directly to physical structural elements within the parking facility.
 * This class tracks individual space metrics, including unique hardware coordinates 
 * and real-time operational availability states.
 */
public class Slot {
    private int slotId;
    private String slotNumber;
    private String status;

    /**
     * Instantiates an active tracking node representation for a physical parking space.
     * By architectural default, slots initialize in a vacant state.
     * 
     * @param slotId     The explicit database row identifier index.
     * @param slotNumber The user-facing zone string tag (e.g., "A1").
     */
    public Slot(int slotId, String slotNumber) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.status = "VACANT"; // All nodes are clear for allocation upon setup
    }

    /**
     * Reads the designator identity string code for frontend display components.
     */
    public String getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the active status value tracking parameter ('VACANT' or 'OCCUPIED').
     */
    public String getStatus() {
        return status;
    }

    /**
     * Mutates structural state transitions when entries or payments complete.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
