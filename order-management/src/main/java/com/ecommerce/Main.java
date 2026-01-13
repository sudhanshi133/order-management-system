package com.ecommerce;

import com.ecommerce.model.*;
import com.ecommerce.service.*;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Create test data
        List<Customer> customers = createTestCustomers();
        List<Product> products = createTestProducts();
        List<Order> orders = createTestOrders();

        // Test Part 1: Functional Programming
        System.out.println("=== PART 1: FUNCTIONAL PROGRAMMING ===\n");
        testProductService(products);
        testCustomerService(customers);
        testOrderService(orders);

        // Test Part 2: Concurrency
        System.out.println("\n=== PART 2: CONCURRENCY ===\n");
        testOrderProcessor(orders, customers);
        testBulkNotifications(customers);
    }

    private static List<Customer> createTestCustomers() {
        return Arrays.asList(
            new Customer(1L, "Alice Johnson", "alice@email.com", "New York", true, 15000.0),
            new Customer(2L, "Bob Smith", "bob@email.com", "Los Angeles", false, 3000.0),
            new Customer(3L, "Carol White", "carol@email.com", "New York", true, 25000.0),
            new Customer(4L, "David Brown", "david@email.com", "Chicago", false, 1500.0),
            new Customer(5L, "Eve Davis", "eve@email.com", "Los Angeles", true, 50000.0),
            new Customer(6L, "Frank Miller", "frank@email.com", "Chicago", false, 800.0),
            new Customer(7L, "Grace Lee", "grace@email.com", "New York", true, 12000.0),
            new Customer(8L, "Henry Wilson", "henry@email.com", "Boston", false, 2200.0)
        );
    }

    private static List<Product> createTestProducts() {
        return Arrays.asList(
            new Product(1L, "Laptop Pro", "Electronics", 1299.99, 50),
            new Product(2L, "Wireless Mouse", "Electronics", 29.99, 200),
            new Product(3L, "USB-C Cable", "Electronics", 12.99, 500),
            new Product(4L, "Office Chair", "Furniture", 299.99, 30),
            new Product(5L, "Standing Desk", "Furniture", 599.99, 15),
            new Product(6L, "Monitor 27\"", "Electronics", 349.99, 75),
            new Product(7L, "Keyboard", "Electronics", 79.99, 150),
            new Product(8L, "Desk Lamp", "Furniture", 45.99, 0), // Out of stock!
            new Product(9L, "Webcam HD", "Electronics", 89.99, 60),
            new Product(10L, "Bookshelf", "Furniture", 149.99, 25)
        );
    }

    private static List<Order> createTestOrders() {
        return Arrays.asList(
            new Order(1L, 1L, Arrays.asList(
                new OrderItem(1L, "Laptop Pro", 1, 1299.99),
                new OrderItem(2L, "Wireless Mouse", 1, 29.99)
            ), LocalDateTime.now().minusDays(1), "CONFIRMED"),
            
            new Order(2L, 2L, Arrays.asList(
                new OrderItem(4L, "Office Chair", 2, 299.99)
            ), LocalDateTime.now().minusDays(5), "SHIPPED"),
            
            new Order(3L, 3L, Arrays.asList(
                new OrderItem(6L, "Monitor 27\"", 2, 349.99),
                new OrderItem(7L, "Keyboard", 1, 79.99)
            ), LocalDateTime.now().minusHours(2), "PENDING"),
            
            new Order(4L, 1L, Arrays.asList(
                new OrderItem(5L, "Standing Desk", 1, 599.99)
            ), LocalDateTime.now().minusDays(10), "DELIVERED"),
            
            new Order(5L, 5L, Arrays.asList(
                new OrderItem(1L, "Laptop Pro", 2, 1299.99),
                new OrderItem(9L, "Webcam HD", 2, 89.99)
            ), LocalDateTime.now(), "PENDING")
        );
    }

    private static void testProductService(List<Product> products) {
        ProductService service = new ProductService(products);
        
        System.out.println("--- Product Service Tests ---");
        System.out.println("Electronics: " + service.getProductsByCategory("Electronics"));
        System.out.println("Price $50-$300: " + service.getProductsInPriceRange(50, 300));
        System.out.println("Names sorted: " + service.getProductNamesSorted());
        System.out.println("Most expensive: " + service.getMostExpensiveProduct());
        System.out.println("Has out of stock: " + service.hasOutOfStockProducts());
        System.out.println("Total inventory value: $" + service.getTotalInventoryValue());
        System.out.println("By category: " + service.getProductsByCategories().keySet());
        System.out.println("Top 3 expensive: " + service.getTopExpensiveProducts(3));
    }

    private static void testCustomerService(List<Customer> customers) {
        CustomerService service = new CustomerService(customers);
        
        System.out.println("\n--- Customer Service Tests ---");
        System.out.println("Premium customers: " + service.getPremiumCustomers());
        System.out.println("NYC customers: " + service.getCustomersByCity("New York"));
        System.out.println("Find by email: " + service.findByEmail("alice@email.com"));
        System.out.println("Email list: " + service.getEmailList());
        System.out.println("Avg premium purchases: $" + service.getAveragePremiumPurchases());
        System.out.println("Top 3 customers: " + service.getTopCustomers(3));
        System.out.println("Count by city: " + service.getCustomerCountByCity());
    }

    private static void testOrderService(List<Order> orders) {
        OrderService service = new OrderService(orders);
        
        System.out.println("\n--- Order Service Tests ---");
        System.out.println("Customer 1 orders: " + service.getOrdersByCustomer(1L));
        System.out.println("Recent orders (7 days): " + service.getRecentOrders(7));
        System.out.println("Total revenue: $" + service.getTotalRevenue());
        System.out.println("Pending orders: " + service.getOrdersByStatus("PENDING"));
        System.out.println("Largest order: " + service.getLargestOrder());
        System.out.println("Count by status: " + service.getOrderCountByStatus());
        System.out.println("Average order value: $" + service.getAverageOrderValue());
    }

    private static void testOrderProcessor(List<Order> orders, List<Customer> customers) {
        OrderProcessor processor = new OrderProcessor();
        Order testOrder = orders.get(0);
        Customer testCustomer = customers.get(0);

        System.out.println("--- Order Processor Tests ---\n");
        
        System.out.println("Processing order SEQUENTIALLY:");
        String tracking1 = processor.processOrderSequential(testOrder, testCustomer);
        System.out.println("Result: " + tracking1 + "\n");

        System.out.println("Processing order CONCURRENTLY:");
        String tracking2 = processor.processOrderConcurrent(testOrder, testCustomer);
        System.out.println("Result: " + tracking2 + "\n");
    }

    private static void testBulkNotifications(List<Customer> customers) {
        BulkNotificationSender sender = new BulkNotificationSender();
        
        System.out.println("--- Bulk Notification Tests ---\n");
        
        System.out.println("Sending emails SEQUENTIALLY:");
        sender.sendPromoEmailsSequential(customers, "Sale!", "50% off everything!");
        
        System.out.println("\nSending emails CONCURRENTLY:");
        sender.sendPromoEmailsConcurrent(customers, "Sale!", "50% off everything!");
    }
}

