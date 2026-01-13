package com.ecommerce.service;

import com.ecommerce.model.Customer;
import java.util.*;
import java.util.concurrent.*;

public class BulkNotificationSender {
    private NotificationService notificationService = new NotificationService();

    /**
     * TASK A: Send promotional email to all customers SEQUENTIALLY
     * Measure time taken.
     */
    public void sendPromoEmailsSequential(List<Customer> customers,
                                          String subject, String body) {
        long startTime = System.currentTimeMillis();
        
        for (Customer customer : customers) {
            notificationService.sendEmail(customer.getEmail(), subject, body);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential emails took: " + (endTime - startTime) + "ms");
    }

    /**
     * TASK B: Send promotional email to all customers CONCURRENTLY
     * Use a thread pool of 10 threads.
     * Measure time taken.
     */
    public void sendPromoEmailsConcurrent(List<Customer> customers,
                                          String subject, String body) {
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        List<Future<?>> futures = new ArrayList<>();
        
        // Submit all email tasks to executor
        for (Customer customer : customers) {
            Future<?> future = executor.submit(() -> 
                notificationService.sendEmail(customer.getEmail(), subject, body)
            );
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
        long endTime = System.currentTimeMillis();
        System.out.println("Concurrent emails took: " + (endTime - startTime) + "ms");
    }

    /**
     * TASK C: Send emails with rate limiting
     *
     * Some email providers limit how many emails you can send per second.
     * Implement sending with a maximum of 5 emails per second.
     *
     * Hint: Use ScheduledExecutorService or add delays
     */
    public void sendPromoEmailsRateLimited(List<Customer> customers,
                                           String subject, String body,
                                           int maxPerSecond) {
        long startTime = System.currentTimeMillis();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(maxPerSecond);
        
        long delayMs = 1000 / maxPerSecond; // Delay between emails in milliseconds
        CountDownLatch latch = new CountDownLatch(customers.size());
        
        for (int i = 0; i < customers.size(); i++) {
            final Customer customer = customers.get(i);
            long delay = i * delayMs;
            
            executor.schedule(() -> {
                try {
                    notificationService.sendEmail(customer.getEmail(), subject, body);
                } finally {
                    latch.countDown();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        
        // Wait for all emails to be sent
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("Rate-limited emails (" + maxPerSecond + "/sec) took: " + (endTime - startTime) + "ms");
    }
}

