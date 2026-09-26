package com.mmu.parking;

public class Application {
    public static void main(String[] args) {
        try {
            // Ignites your custom native web framework wrapper completely offline
            ParkingController.startServer();
        } catch (Exception e) {
            System.out.println("❌ Failed to initialize offline server pipeline container.");
            e.printStackTrace();
        }
    }
}
