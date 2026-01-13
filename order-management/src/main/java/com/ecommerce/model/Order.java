package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    private Long customerId;
    private List<OrderItem> items;
    private LocalDateTime orderDate;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    public Order(Long id, Long customerId, List<OrderItem> items,
                 LocalDateTime orderDate, String status) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", customerId=" + customerId +
               ", total=" + getTotalAmount() + ", status='" + status + "'}";
    }
}

