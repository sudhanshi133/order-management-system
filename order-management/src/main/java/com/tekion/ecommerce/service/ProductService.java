package com.ecommerce.service;

import com.ecommerce.model.Product;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductService {
    private List<Product> products;

    public ProductService(List<Product> products) {
        this.products = products;
    }

    // 1. Find all products in a specific category
    public List<Product> getProductsByCategory(String category) {
        return products.stream()
                .filter(p -> Objects.equals(p.getCategory(), category))
                .collect(Collectors.toList());
    }

    // 2. Find all products with price between min and max (inclusive)
    public List<Product> getProductsInPriceRange(double min, double max) {
        return products.stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(Collectors.toList());
    }

    // 3. Get product names sorted alphabetically
    public List<String> getProductNamesSorted() {
        return products.stream()
                .map(Product::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    // 4. Find the most expensive product (return Optional)
    public Optional<Product> getMostExpensiveProduct() {
        return products.stream()
                .max(Comparator.comparingDouble(Product::getPrice));
    }

    // 5. Check if any product is out of stock (stockQuantity == 0)
    public boolean hasOutOfStockProducts() {
        return products.stream()
                .anyMatch(p -> p.getStockQuantity() == 0);
    }

    // 6. Get total value of all inventory (price * stockQuantity for each)
    public double getTotalInventoryValue() {
        return products.stream()
                .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
                .sum();
    }

    // 7. Group products by category
    public Map<String, List<Product>> getProductsByCategories() {
        return products.stream()
                .collect(Collectors.groupingBy(Product::getCategory));
    }

    // 8. Get top N most expensive products
    public List<Product> getTopExpensiveProducts(int n) {
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // 9. Apply discount to all products in a category
    //    Return new list with discounted prices (don't modify original)
    public List<Product> applyDiscountToCategory(String category, double discountPercent) {
        double factor = 1.0 - (discountPercent / 100.0);
        return products.stream()
                .filter(p -> Objects.equals(p.getCategory(), category))
                .map(p -> new Product(
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        p.getPrice() * factor,
                        p.getStockQuantity()))
                .collect(Collectors.toList());
    }

    // 10. BONUS: Find products matching a custom predicate
    public List<Product> findProducts(Predicate<Product> criteria) {
        return products.stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
}

