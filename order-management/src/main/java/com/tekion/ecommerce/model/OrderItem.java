package com.ecommerce.model;

public class OrderItem {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(Long productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return productName + " x" + quantity;
    }
}

