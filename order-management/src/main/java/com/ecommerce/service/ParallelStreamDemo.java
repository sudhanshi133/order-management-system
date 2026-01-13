package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BONUS 4: Compare parallelStream() vs regular stream() performance
 */
public class ParallelStreamDemo {
    private static final Logger logger = LoggerFactory.getLogger(ParallelStreamDemo.class);

    /**
     * Simulate expensive computation (e.g., price calculation with external API call)
     */
    private double expensiveComputation(Product product) {
        try {
            // Simulate 10ms processing time per product
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Calculate discounted price with tax
        return product.getPrice() * 0.9 * 1.08;
    }

    /**
     * Process products using regular sequential stream
     */
    public List<Double> processWithSequentialStream(List<Product> products) {
        long startTime = System.currentTimeMillis();
        
        List<Double> results = products.stream()
            .map(this::expensiveComputation)
            .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Sequential stream processed {} products in {}ms", products.size(), duration);
        
        return results;
    }

    /**
     * Process products using parallel stream
     */
    public List<Double> processWithParallelStream(List<Product> products) {
        long startTime = System.currentTimeMillis();
        
        List<Double> results = products.parallelStream()
            .map(this::expensiveComputation)
            .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Parallel stream processed {} products in {}ms", products.size(), duration);
        
        return results;
    }

    /**
     * Compare performance with different dataset sizes
     */
    public void comparePerformance() {
        int[] sizes = {10, 50, 100, 500, 1000};
        
        System.out.println("\n=== PARALLEL STREAM PERFORMANCE COMPARISON ===\n");
        System.out.println("Dataset Size | Sequential | Parallel | Speedup");
        System.out.println("-------------|------------|----------|--------");
        
        for (int size : sizes) {
            List<Product> products = generateProducts(size);
            
            // Warm up JVM
            processWithSequentialStream(products.subList(0, Math.min(10, size)));
            processWithParallelStream(products.subList(0, Math.min(10, size)));
            
            // Measure sequential
            long seqStart = System.currentTimeMillis();
            processWithSequentialStream(products);
            long seqDuration = System.currentTimeMillis() - seqStart;
            
            // Measure parallel
            long parStart = System.currentTimeMillis();
            processWithParallelStream(products);
            long parDuration = System.currentTimeMillis() - parStart;
            
            double speedup = (double) seqDuration / parDuration;
            
            System.out.printf("%12d | %8dms | %6dms | %.2fx%n", 
                size, seqDuration, parDuration, speedup);
        }
        
        System.out.println("\nNote: Parallel streams are beneficial for:");
        System.out.println("  - Large datasets (1000+ elements)");
        System.out.println("  - CPU-intensive operations");
        System.out.println("  - Independent computations (no shared state)");
        System.out.println("\nAvoid parallel streams for:");
        System.out.println("  - Small datasets (overhead > benefit)");
        System.out.println("  - I/O-bound operations (use ExecutorService instead)");
        System.out.println("  - Operations with side effects or shared mutable state");
    }

    /**
     * Generate test products
     */
    private List<Product> generateProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(new Product(
                (long) i,
                "Product " + i,
                i % 2 == 0 ? "Electronics" : "Furniture",
                10.0 + (i % 100),
                100
            ));
        }
        return products;
    }

    /**
     * Demonstrate when NOT to use parallel streams
     */
    public void demonstratePitfalls() {
        System.out.println("\n=== PARALLEL STREAM PITFALLS ===\n");
        
        List<Product> products = generateProducts(100);
        
        // PITFALL 1: Shared mutable state (WRONG!)
        System.out.println("1. Shared Mutable State (INCORRECT):");
        List<String> sharedList = new ArrayList<>(); // NOT thread-safe!
        
        // This is WRONG - race condition!
        // products.parallelStream().forEach(p -> sharedList.add(p.getName()));
        
        // CORRECT way:
        List<String> correctList = products.parallelStream()
            .map(Product::getName)
            .collect(Collectors.toList());
        System.out.println("   Use collect() instead of forEach() with shared state");
        
        // PITFALL 2: Order matters
        System.out.println("\n2. Order Dependency:");
        System.out.println("   Sequential: " + products.stream().limit(5).map(Product::getName).collect(Collectors.toList()));
        System.out.println("   Parallel:   " + products.parallelStream().limit(5).map(Product::getName).collect(Collectors.toList()));
        System.out.println("   Note: Parallel streams may not preserve encounter order");
        
        // PITFALL 3: Small datasets
        System.out.println("\n3. Small Dataset Overhead:");
        List<Product> smallList = generateProducts(5);
        
        long seqTime = measureTime(() -> smallList.stream().map(Product::getPrice).collect(Collectors.toList()));
        long parTime = measureTime(() -> smallList.parallelStream().map(Product::getPrice).collect(Collectors.toList()));
        
        System.out.printf("   Sequential: %dms, Parallel: %dms%n", seqTime, parTime);
        System.out.println("   Parallel overhead makes it slower for small datasets!");
    }

    private long measureTime(Runnable operation) {
        long start = System.currentTimeMillis();
        operation.run();
        return System.currentTimeMillis() - start;
    }
}

