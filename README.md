# DATA STRUCTURES AND ALGORITHMS — TASK ONE
**Course Assignment:** Modern Parking Automation System (Kenya)
**Target Institution:** Multimedia University of Kenya
**Development Environment:** Windows 11 / Java Native Engine Core

---

## 1. SYSTEM MODULE ANALYSIS & ALGORITHMS

The Jipange Parking Automation system is broken down into four core modules to handle all the automated project requirements.

### Module 1: Live Space Allocation & Visual Display Monitor
*   **Functional Purpose:** Scans the parking space array list to calculate remaining spaces and updates the real-time visual grid layout on the frontend dashboard before a driver enters.
*   **Algorithm (Formal Pseudocode):**
```text
BEGIN MODULE_VisualDisplay Monitor
    INITIALIZE vacant_slots_counter TO 0
    FETCH current_slots_layout FROM Array_Structure (layout)
    
    FOR EACH slot IN current_slots_layout DO
        IF slot.status EQUALS "VACANT" THEN
            RENDER slot with EMERALD_GREEN styling UI property
            INCREMENT vacant_slots_counter BY 1
        ELSE
            RENDER slot with ROSE_RED styling UI property
        ENDIF
    ENDFOR
    
    UPDATE DOM_Element("available-counter") WITH vacant_slots_counter
    
    IF vacant_slots_counter EQUALS 0 THEN
        DISPLAY "PARKING FACILITY AT MAXIMUM CAPACITY"
        DISABLE Entry_Trigger_Button
    ENDIF
END MODULE_VisualDisplay
```

### Module 2: Vehicle Intake & Ingestion Logging Engine
*   **Functional Purpose:** Intercepts incoming license plates at the entry barrier, saves a high-precision entry timestamp, assigns an available parking slot, and syncs the records to the database.
*   **Algorithm (Formal Pseudocode):**
```text
BEGIN MODULE_VehicleCheckIn(license_plate)
    IF active_vehicles_map CONTAINS KEY license_plate THEN
        RETURN "ERROR: Vehicle already logged inside the facility."
    ENDIF
    
    INITIALIZE target_slot TO NULL
    FETCH complete_layout_array FROM layout
    
    FOR EACH slot IN complete_layout_array DO
        IF slot.status EQUALS "VACANT" THEN
            target_slot = slot
            BREAK LOOP
        ENDIF
    ENDFOR
    
    IF target_slot IS NULL THEN
        RETURN "REGRET: Automation barrier blocked. Facility Full."
    ENDIF
    
    SET target_slot.status TO "OCCUPIED"
    CREATE instantiated_object Vehicle(license_plate, target_slot.slot_number, CURRENT_TIMESTAMP)
    
    INSERT INTO active_vehicles_map (KEY=license_plate, VALUE=instantiated_object)
    
    START DATABASE_TRANSACTION
        EXECUTE "INSERT INTO vehicle_logs (slot_id, license_plate, entry_time) VALUES (?, ?, ?)"
        EXECUTE "UPDATE parking_slots SET status = 'OCCUPIED' WHERE slot_number = ?"
    COMMIT DATABASE_TRANSACTION
    
    TRIGGER_BARRIER_HARDWARE("OPEN_GATE")
    RETURN "Welcome! Proceed to Slot: " + target_slot.slot_number
END MODULE_VehicleCheckIn
```

### Module 3: Exit Processing & Tiered Tariff Calculator
*   **Functional Purpose:** Launches checkout calculations when a vehicle arrives at the exit gate, computes the total minutes spent inside, and matches it against the project's pricing metrics.
*   **Algorithm (Formal Pseudocode):**
```text
BEGIN MODULE_TariffCalculator(license_plate)
    IF active_vehicles_map DOES NOT CONTAIN KEY license_plate THEN
        RETURN "ERROR: Target license plate untracked in active database."
    ENDIF
    
    FETCH vehicle_record FROM active_vehicles_map USING KEY license_plate
    SET reference_entry_time TO vehicle_record.entry_time
    SET current_exit_time TO GET_SYSTEM_LOCAL_DATE_TIME()
    
    COMPUTE elapsed_minutes TO DIFFERENCE_IN_MINUTES(reference_entry_time, current_exit_time)
    INITIALIZE amount_payable TO 0.0
    
    IF elapsed_minutes <= 30 THEN
        amount_payable = 0.0
    ELSE IF elapsed_minutes <= 120 THEN
        amount_payable = 50.0
    ELSE IF elapsed_minutes <= 240 THEN
        amount_payable = 100.0
    ELSE IF elapsed_minutes <= 360 THEN
        amount_payable = 300.0
    ELSE
        amount_payable = 500.0
    ENDIF
    
    RETURN amount_payable
END MODULE_TariffCalculator
```

### Module 4: Integrated M-Pesa Payment Handshake & Barrier Control
*   **Functional Purpose:** Simulates a connection with Safaricom’s Daraja API to verify that the parking payment goes through before clearing the car from memory cache tracking tables and opening the exit gate.
*   **Algorithm (Formal Pseudocode):**
```text
BEGIN MODULE_PaymentBarrierControl(license_plate, customer_phone)
    COMPUTE total_due_balance TO CALL MODULE_TariffCalculator(license_plate)
    
    IF total_due_balance > 0.0 THEN
        INITIALIZE mpesa_request TO DISPATCH_DARAJA_STK_PUSH(customer_phone, total_due_balance)
        POLL mpesa_request FOR response_status UNTIL CALLBACK_RECEIVED_FROM_SAFARICOM
        
        IF response_status NOT EQUALS "SUCCESS" THEN
            RETURN "CHECKOUT DENIED: Outstanding structural invoice settlement required."
        ENDIF
    ENDIF
    
    FETCH vehicle_record FROM active_vehicles_map USING KEY license_plate
    SET occupied_slot_code TO vehicle_record.slot_assigned
    
    FOR EACH slot IN layout DO
        IF slot.slot_number EQUALS occupied_slot_code THEN
            SET slot.status TO "VACANT"
            BREAK LOOP
        ENDIF
    ENDFOR
    
    REMOVE license_plate FROM active_vehicles_map
    
    START DATABASE_TRANSACTION
        EXECUTE "UPDATE vehicle_logs SET exit_time = ?, amount_paid = ? WHERE license_plate = ? AND exit_time IS NULL"
        EXECUTE "UPDATE parking_slots SET status = 'VACANT' WHERE slot_number = ?"
    COMMIT DATABASE_TRANSACTION
    
    TRIGGER_BARRIER_HARDWARE("OPEN_GATE")
    RETURN "Transaction Verified. Safe Journeys."
END MODULE_PaymentBarrierControl
```

---

## 2. DATA STRUCTURE ANALYSIS & SELECTION RATIONALE

To make sure the system executes quickly and efficiently, specific Java collections were selected based on their algorithmic time complexity and performance traits.

### A. Dynamic Array Lists (`java.util.ArrayList<Slot>`)
*   **Use in System:** Used to hold the sequential list of all 100 physical parking spaces.
*   **Rationale:** The application needs to iterate through all 100 slots in order when rendering the grid cells on the user interface dashboard. An `ArrayList` preserves the index insertion order perfectly and provides fast sequential access across adjacent memory locations.

### B. Hash Maps (`java.util.HashMap<String, Vehicle>`)
*   **Use in System:** Tracks cars currently parked inside the facility using their unique vehicle license plate string as the lookup key.
*   **Rationale:** A busy entry or exit barrier cannot afford slow linear searches (\(O(n)\) time) when checking if a vehicle is registered or calculating its exit fee. By using a hash index map, lookup times drop to a constant **\(O(1)\) runtime complexity**, allowing instant retrieval regardless of how many cars are inside.

---

## 3. RELATIONAL DATABASE SETUP SCHEMA

This script is structured for relational database managers (such as XAMPP / MySQL Server) to initialize the backend tables.

```sql
-- Database Initialization Script
CREATE DATABASE IF NOT EXISTS parking_sys;
USE parking_sys;

-- Table Structure 1: Stores info about the parking slot statuses
CREATE TABLE IF NOT EXISTS parking_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'VACANT'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table Structure 2: Stores transactional log details for vehicles checking in and out
CREATE TABLE IF NOT EXISTS vehicle_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_id INT,
    license_plate VARCHAR(20) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME DEFAULT NULL,
    amount_paid DECIMAL(10, 2) DEFAULT 0.0,
    mpesa_checkout_id VARCHAR(100) DEFAULT NULL,
    INDEX idx_active_plates (license_plate, exit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
