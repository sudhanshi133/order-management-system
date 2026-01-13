package com.ecommerce.service;

import com.ecommerce.external.*;
import com.ecommerce.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * BONUS: Enhanced OrderProcessor with error handling, retry logic, and timeouts
 */
public class RobustOrderProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RobustOrderProcessor.class);
    
    private PaymentGateway paymentGateway = new PaymentGateway();
    private InventorySystem inventorySystem = new InventorySystem();
    private ShippingProvider shippingProvider = new ShippingProvider();
    private NotificationService notificationService = new NotificationService();
    
    private static final int MAX_RETRIES = 3;
    private static final long TIMEOUT_SECONDS = 5;

    /**
     * BONUS 1: Error handling with rollback
     * If payment fails after inventory is checked, we need to handle it gracefully
     */
    public String processOrderWithRollback(Order order, Customer customer) {
        long startTime = System.currentTimeMillis();
        boolean inventoryReserved = false;
        boolean paymentProcessed = false;
        
        try {
            logger.info("Starting order processing for order {}", order.getId());
            
            // Step 1: Check inventory
            if (!inventorySystem.checkAvailability(order.getId())) {
                logger.warn("Order {} failed: Inventory not available", order.getId());
                return null;
            }
            
            // Step 2: Reserve inventory
            inventorySystem.reserveInventory(order.getId());
            inventoryReserved = true;
            logger.info("Inventory reserved for order {}", order.getId());
            
            // Step 3: Process payment (might fail!)
            if (!paymentGateway.processPayment(order.getId(), order.getTotalAmount())) {
                logger.error("Payment failed for order {}, rolling back inventory", order.getId());
                // ROLLBACK: Release the reserved inventory
                inventorySystem.releaseInventory(order.getId());
                inventoryReserved = false;
                return null;
            }
            paymentProcessed = true;
            logger.info("Payment processed for order {}", order.getId());
            
            // Step 4: Get shipping quote and schedule pickup
            double shippingCost = shippingProvider.getShippingQuote(order.getId(), customer.getCity());
            String trackingNumber = shippingProvider.schedulePickup(order.getId());
            
            // Step 5: Send confirmation
            notificationService.sendEmail(
                customer.getEmail(),
                "Order Confirmation #" + order.getId(),
                "Your order has been confirmed. Tracking: " + trackingNumber
            );
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Order {} processed successfully in {}ms", order.getId(), duration);
            return trackingNumber;
            
        } catch (Exception e) {
            logger.error("Error processing order {}: {}", order.getId(), e.getMessage(), e);
            
            // ROLLBACK: Clean up any partial state
            if (inventoryReserved && !paymentProcessed) {
                logger.info("Rolling back inventory reservation for order {}", order.getId());
                inventorySystem.releaseInventory(order.getId());
            }
            
            return null;
        }
    }

    /**
     * BONUS 2: Retry logic - retry up to 3 times on failure
     */
    public <T> T executeWithRetry(Callable<T> operation, String operationName) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRIES) {
            attempts++;
            try {
                logger.debug("Attempting {} (attempt {}/{})", operationName, attempts, MAX_RETRIES);
                T result = operation.call();
                if (attempts > 1) {
                    logger.info("{} succeeded on attempt {}", operationName, attempts);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                logger.warn("{} failed on attempt {}: {}", operationName, attempts, e.getMessage());
                
                if (attempts < MAX_RETRIES) {
                    // Exponential backoff: wait 100ms, 200ms, 400ms
                    long waitTime = 100L * (1L << (attempts - 1));
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.error("{} failed after {} attempts", operationName, MAX_RETRIES);
        throw new RuntimeException("Operation failed after " + MAX_RETRIES + " retries", lastException);
    }

    /**
     * BONUS 3: Timeout handling - cancel if operation takes too long
     */
    public String processOrderWithTimeout(Order order, Customer customer) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            logger.info("Processing order {} with {}s timeout", order.getId(), TIMEOUT_SECONDS);
            
            Future<String> future = executor.submit(() -> {
                // Check inventory with retry
                Boolean inventoryAvailable = executeWithRetry(
                    () -> inventorySystem.checkAvailability(order.getId()),
                    "Inventory check for order " + order.getId()
                );
                
                if (!inventoryAvailable) {
                    return null;
                }
                
                // Process payment with retry
                Boolean paymentSuccess = executeWithRetry(
                    () -> paymentGateway.processPayment(order.getId(), order.getTotalAmount()),
                    "Payment for order " + order.getId()
                );
                
                if (!paymentSuccess) {
                    return null;
                }
                
                // Reserve inventory
                inventorySystem.reserveInventory(order.getId());
                
                // Get shipping and schedule pickup
                shippingProvider.getShippingQuote(order.getId(), customer.getCity());
                String trackingNumber = shippingProvider.schedulePickup(order.getId());
                
                // Send email asynchronously
                CompletableFuture.runAsync(() -> 
                    notificationService.sendEmail(
                        customer.getEmail(),
                        "Order Confirmation #" + order.getId(),
                        "Your order has been confirmed. Tracking: " + trackingNumber
                    )
                );
                
                return trackingNumber;
            });
            
            // Wait for result with timeout
            String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.info("Order {} completed within timeout", order.getId());
            return result;
            
        } catch (TimeoutException e) {
            logger.error("Order {} timed out after {}s", order.getId(), TIMEOUT_SECONDS);
            return null;
        } catch (Exception e) {
            logger.error("Error processing order {}: {}", order.getId(), e.getMessage(), e);
            return null;
        } finally {
            executor.shutdownNow();
        }
    }
}

