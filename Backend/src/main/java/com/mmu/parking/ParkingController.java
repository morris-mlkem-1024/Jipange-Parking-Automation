package com.mmu.parking;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * ARCHITECTURAL LAYER: Controller / Web Routing Layer
 * PURPOSE: Initializes an embedded network socket server pipeline. It captures cross-origin (CORS) 
 * browser requests, dispatches parameters to our internal ParkingManagement engine, and pushes back responses.
 */
public class ParkingController {

    // Initializing our core backend engine instance with a default allocation of 100 parking bays
    private static final ParkingManagement parkingSystem = new ParkingManagement(100);

    /**
     * Spins up the embedded HTTP web listener engine on a specified port.
     * Maps explicit routing path layouts to support asynchronous operations from the HTML frontend dashboard.
     */
    public static void startServer() throws IOException {
        // Bind to all local network loopback interfaces using default port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Unified catch-all context routing path to process API web streams efficiently
        server.createContext("/api/parking/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // APPLIES EXPLICIT CORS BYPASS: Prevents modern web browsers from blocking local inter-port communications
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

                // Instantly intercept and validate browser preflight check negotiations before handling payloads
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                String method = exchange.getRequestMethod();
                String response = "";

                try {
                    // ROUTE 1: Intake operations mapping endpoint
                    if (path.endsWith("/checkin") && "POST".equalsIgnoreCase(method)) {
                        String plate = getQueryParam(query, "plate");
                        response = parkingSystem.checkIn(plate);
                        sendResponse(exchange, response, 200);
                    } 
                    // ROUTE 2: Outtake operations mapping endpoint
                    else if (path.endsWith("/checkout") && "POST".equalsIgnoreCase(method)) {
                        String plate = getQueryParam(query, "plate");
                        response = parkingSystem.checkOut(plate);
                        sendResponse(exchange, response, 200);
                    } 
                    // ROUTE 3: Live real-time visualization tracker grid data model stream endpoint
                    else if (path.endsWith("/slots") && "GET".equalsIgnoreCase(method)) {
                        ArrayList<Slot> slots = parkingSystem.getLayout();
                        StringBuilder json = new StringBuilder("[");
                        
                        // Parse Java model properties objects sequentially to generate a structured JSON data string array
                        for (int i = 0; i < slots.size(); i++) {
                            Slot s = slots.get(i);
                            json.append(String.format("{\"slotNumber\":\"%s\",\"status\":\"%s\"}", s.getSlotNumber(), s.getStatus()));
                            if (i < slots.size() - 1) json.append(",");
                        }
                        json.append("]");
                        
                        // Inform browser endpoints that they are downloading standard application/json strings packets
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        sendResponse(exchange, json.toString(), 200);
                    } 
                    // ROUTE 4: Fallback handle block
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

        // Use standard thread execution managers allocations schedules sets
        server.setExecutor(null);
        server.start();
        System.out.println("🚀 Jipange Parking Core Native Engine listening actively on http://localhost:8080");
    }

    /**
     * Helper method to output byte streams back across active TCP server connections pathways.
     */
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Extracts exact target string parameters keys from standard HTTP URI query string blocks maps.
     */
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
