package com.ecommerce.service;

import com.ecommerce.external.*;
import com.ecommerce.model.*;
import java.util.concurrent.*;
import java.util.*;

public class OrderProcessor {
    private PaymentGateway paymentGateway = new PaymentGateway();
    private InventorySystem inventorySystem = new InventorySystem();
    private ShippingProvider shippingProvider = new ShippingProvider();
    private NotificationService notificationService = new NotificationService();

    /**
     * TASK A: Process order SEQUENTIALLY
     *
     * Steps (must all succeed):
     * 1. Check inventory availability
     * 2. Process payment
     * 3. Reserve inventory
     * 4. Get shipping quote
     * 5. Schedule pickup
     * 6. Send confirmation email
     *
     * Measure and print total time taken.
     * Return tracking number if successful, null if failed.
     */
    public String processOrderSequential(Order order, Customer customer) {
        long startTime = System.currentTimeMillis();
        
        // Step 1: Check inventory availability
        if (!inventorySystem.checkAvailability(order.getId())) {
            long endTime = System.currentTimeMillis();
            System.out.println("Sequential processing took: " + (endTime - startTime) + "ms");
            return null;
        }
        
        // Step 2: Process payment
        if (!paymentGateway.processPayment(order.getId(), order.getTotalAmount())) {
            long endTime = System.currentTimeMillis();
            System.out.println("Sequential processing took: " + (endTime - startTime) + "ms");
            return null;
        }
        
        // Step 3: Reserve inventory
        inventorySystem.reserveInventory(order.getId());
        
        // Step 4: Get shipping quote
        double shippingCost = shippingProvider.getShippingQuote(order.getId(), customer.getCity());
        
        // Step 5: Schedule pickup
        String trackingNumber = shippingProvider.schedulePickup(order.getId());
        
        // Step 6: Send confirmation email
        notificationService.sendEmail(
            customer.getEmail(),
            "Order Confirmation #" + order.getId(),
            "Your order has been confirmed. Tracking: " + trackingNumber
        );
        
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential processing took: " + (endTime - startTime) + "ms");
        return trackingNumber;
    }

    /**
     * TASK B: Process order with CONCURRENT independent operations
     *
     * Analyze the steps above:
     * - Which steps can run in parallel?
     * - Which steps depend on others?
     *
     * Optimize by running independent operations concurrently.
     * Use ExecutorService and Future.
     *
     * Measure and print total time taken.
     */
    public String processOrderConcurrent(Order order, Customer customer) {
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
            // Run inventory check, payment, and shipping quote in parallel
            Future<Boolean> inventoryFuture = executor.submit(() -> 
                inventorySystem.checkAvailability(order.getId())
            );
            
            Future<Boolean> paymentFuture = executor.submit(() -> 
                paymentGateway.processPayment(order.getId(), order.getTotalAmount())
            );
            
            Future<Double> shippingQuoteFuture = executor.submit(() -> 
                shippingProvider.getShippingQuote(order.getId(), customer.getCity())
            );
            
            // Wait for all three to complete
            boolean inventoryAvailable = inventoryFuture.get();
            boolean paymentSuccess = paymentFuture.get();
            double shippingCost = shippingQuoteFuture.get();
            
            // If either inventory or payment failed, abort
            if (!inventoryAvailable || !paymentSuccess) {
                return null;
            }
            
            // Now do dependent operations sequentially
            inventorySystem.reserveInventory(order.getId());
            String trackingNumber = shippingProvider.schedulePickup(order.getId());
            
            // Send email asynchronously (fire and forget)
            executor.submit(() -> 
                notificationService.sendEmail(
                    customer.getEmail(),
                    "Order Confirmation #" + order.getId(),
                    "Your order has been confirmed. Tracking: " + trackingNumber
                )
            );
            
            return trackingNumber;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            executor.shutdown();
            long endTime = System.currentTimeMillis();
            System.out.println("Concurrent processing took: " + (endTime - startTime) + "ms");
        }
    }

    /**
     * TASK C: Process MULTIPLE orders concurrently
     *
     * Given a list of orders, process them all concurrently.
     * Use a thread pool to limit concurrent processing.
     *
     * Return a Map of orderId -> trackingNumber (null if failed)
     */
    public Map<Long, String> processMultipleOrders(List<Order> orders,
                                                    Map<Long, Customer> customerMap) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<Long, String> results = new ConcurrentHashMap<>();
        
        List<Future<?>> futures = new ArrayList<>();
        
        // Submit all orders for processing
        for (Order order : orders) {
            Future<?> future = executor.submit(() -> {
                Customer customer = customerMap.get(order.getCustomerId());
                if (customer != null) {
                    String tracking = processOrderConcurrent(order, customer);
                    results.put(order.getId(), tracking);
                }
            });
            futures.add(future);
        }
        
        // Wait for all to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        return results;
    }

    /**
     * BONUS TASK: Rewrite processOrderConcurrent using CompletableFuture
     *
     * Benefits:
     * - No need to manage ExecutorService manually
     * - Chain operations with thenApply, thenCompose
     * - Combine results with thenCombine, allOf
     * - Handle errors with exceptionally
     */
    public CompletableFuture<String> processOrderAsync(Order order, Customer customer) {
        // Run inventory check and payment in parallel
        CompletableFuture<Boolean> inventoryCheck = CompletableFuture.supplyAsync(() -> 
            inventorySystem.checkAvailability(order.getId())
        );
        
        CompletableFuture<Boolean> paymentResult = CompletableFuture.supplyAsync(() -> 
            paymentGateway.processPayment(order.getId(), order.getTotalAmount())
        );
        
        CompletableFuture<Double> shippingQuote = CompletableFuture.supplyAsync(() -> 
            shippingProvider.getShippingQuote(order.getId(), customer.getCity())
        );
        
        // Combine inventory and payment results
        return inventoryCheck
            .thenCombine(paymentResult, (inv, pay) -> inv && pay)
            .thenCompose(success -> {
                if (!success) {
                    return CompletableFuture.completedFuture(null);
                }
                
                // Reserve inventory
                inventorySystem.reserveInventory(order.getId());
                
                // Schedule pickup
                String trackingNumber = shippingProvider.schedulePickup(order.getId());
                
                // Send email asynchronously
                CompletableFuture.runAsync(() -> 
                    notificationService.sendEmail(
                        customer.getEmail(),
                        "Order Confirmation #" + order.getId(),
                        "Your order has been confirmed. Tracking: " + trackingNumber
                    )
                );
                
                return CompletableFuture.completedFuture(trackingNumber);
            })
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }
}

