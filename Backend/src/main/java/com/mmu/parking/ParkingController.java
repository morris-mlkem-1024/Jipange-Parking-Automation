package com.mmu.parking;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

// Set up the web routing endpoints so the frontend dashboard can talk to the Java logic
public class ParkingController {

    // Create the parking management engine instance with a layout of 100 slots
    private static final ParkingManagement parkingSystem = new ParkingManagement(100);

    // Boot up the embedded Java HTTP server listener on port 8080
    public static void startServer() throws IOException {
        // Open port 8080 to listen for incoming connections
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Define the main context path route for our web service calls
        server.createContext("/api/parking/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Set CORS headers so the web page browser doesn't block data access
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

                // Handle HTTP OPTIONS preflight checks sent by the browser
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                String method = exchange.getRequestMethod();
                String response = "";

                try {
                    // Check-in Route: Handles incoming vehicle registration plates
                    if (path.endsWith("/checkin") && "POST".equalsIgnoreCase(method)) {
                        String plate = getQueryParam(query, "plate");
                        response = parkingSystem.checkIn(plate);
                        sendResponse(exchange, response, 200);
                    } 
                    // Check-out Route: Calculates total fees and checks M-Pesa status
                    else if (path.endsWith("/checkout") && "POST".equalsIgnoreCase(method)) {
                        String plate = getQueryParam(query, "plate");
                        response = parkingSystem.checkOut(plate);
                        sendResponse(exchange, response, 200);
                    } 
                    // Slots Route: Serializes the active grid arrays tracking states into JSON data
                    else if (path.endsWith("/slots") && "GET".equalsIgnoreCase(method)) {
                        ArrayList<Slot> slots = parkingSystem.getLayout();
                        StringBuilder json = new StringBuilder("[");
                        
                        // Loop through our slot objects to build a clean JSON format text string
                        for (int i = 0; i < slots.size(); i++) {
                            Slot s = slots.get(i);
                            json.append(String.format("{\"slotNumber\":\"%s\",\"status\":\"%s\"}", s.getSlotNumber(), s.getStatus()));
                            if (i < slots.size() - 1) json.append(",");
                        }
                        json.append("]");
                        
                        // Set standard json content properties response header headers
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        sendResponse(exchange, json.toString(), 200);
                    } 
                    // Catch unknown endpoints
                    else {
                        sendResponse(exchange, "Resource Path Not Found", 404);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Web Router encountered processing exception on route: " + path);
                    e.printStackTrace();
                    sendResponse(exchange, "Internal Server Error: " + e.getMessage(), 500);
                }
            }
        });

        // Set default system thread pool parameters configurations
        server.setExecutor(null);
        server.start();
        System.out.println("🚀 Jipange Parking Core Native Engine listening actively on http://localhost:8080");
    }

    // Helper method to write response character data streams back to the browser network connection
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Utility method to parse target parameters from the address query key fields
    private static String getQueryParam(String query, String key) {
        if (query == null) return "";
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equalsIgnoreCase(key)) {
                return pair[1];
            }
        }
        return "";
    }
}
