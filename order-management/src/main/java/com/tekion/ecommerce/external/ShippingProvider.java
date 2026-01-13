package com.ecommerce.external;

public class ShippingProvider {
    /**
     * Simulates getting shipping quote - takes 400ms
     */
    public double getShippingQuote(Long orderId, String city) {
        try {
            System.out.println("[Shipping] Getting quote for order " + orderId + " to " + city + "...");
            Thread.sleep(400);
            double quote = 5.0 + Math.random() * 15.0; // $5-$20
            System.out.println("[Shipping] Order " + orderId + ": $" + String.format("%.2f", quote));
            return quote;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Simulates scheduling pickup - takes 300ms
     */
    public String schedulePickup(Long orderId) {
        try {
            System.out.println("[Shipping] Scheduling pickup for order " + orderId + "...");
            Thread.sleep(300);
            String trackingNumber = "TRK" + System.currentTimeMillis();
            System.out.println("[Shipping] Order " + orderId + ": Tracking# " + trackingNumber);
            return trackingNumber;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

