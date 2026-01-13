package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderService {
    private List<Order> orders;

    public OrderService(List<Order> orders) {
        this.orders = orders;
    }

    // 1. Get all orders for a specific customer
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orders.stream()
                .filter(o -> Objects.equals(o.getCustomerId(), customerId))
                .collect(Collectors.toList());
    }

    // 2. Get orders placed in the last N days
    public List<Order> getRecentOrders(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return orders.stream()
                .filter(o -> !o.getOrderDate().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    // 3. Get total revenue (sum of all order amounts)
    public double getTotalRevenue() {
        return orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    // 4. Get orders by status, sorted by date (newest first)
    public List<Order> getOrdersByStatus(String status) {
        return orders.stream()
                .filter(o -> Objects.equals(o.getStatus(), status))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    // 5. Get the order with highest total amount
    public Optional<Order> getLargestOrder() {
        return orders.stream()
                .max(Comparator.comparingDouble(Order::getTotalAmount));
    }

    // 6. Count orders by status
    public Map<String, Long> getOrderCountByStatus() {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    // 7. Get all unique product IDs that have been ordered
    public Set<Long> getAllOrderedProductIds() {
        return orders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());
    }

    // 8. Calculate average order value
    public double getAverageOrderValue() {
        return orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .average()
                .orElse(0.0);
    }
}

