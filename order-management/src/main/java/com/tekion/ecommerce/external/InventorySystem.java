package com.ecommerce.external;

public class InventorySystem {
    /**
     * Simulates inventory check - takes 300ms
     * Returns true if all items available
     */
    public boolean checkAvailability(Long orderId) {
        try {
            System.out.println("[Inventory] Checking stock for order " + orderId + "...");
            Thread.sleep(300);
            boolean available = Math.random() > 0.05; // 95% availability
            System.out.println("[Inventory] Order " + orderId + ": " + (available ? "IN STOCK" : "OUT OF STOCK"));
            return available;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Simulates reserving inventory - takes 200ms
     */
    public void reserveInventory(Long orderId) {
        try {
            System.out.println("[Inventory] Reserving items for order " + orderId + "...");
            Thread.sleep(200);
            System.out.println("[Inventory] Order " + orderId + ": RESERVED");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

