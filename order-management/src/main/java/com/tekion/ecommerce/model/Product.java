package com.ecommerce.model;

public class Product {
    private Long id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;

    public Product(Long id, String name, String category, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }

    public void setStockQuantity(int qty) { this.stockQuantity = qty; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}

