package com.ecommerce.external;

public class PaymentGateway {
    /**
     * Simulates payment processing - takes 500ms
     * Returns true if payment successful, false otherwise
     */
    public boolean processPayment(Long orderId, double amount) {
        try {
            System.out.println("[Payment] Processing payment for order " + orderId + "...");
            Thread.sleep(500); // Simulate network delay
            boolean success = Math.random() > 0.1; // 90% success rate
            System.out.println("[Payment] Order " + orderId + ": " + (success ? "SUCCESS" : "FAILED"));
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

