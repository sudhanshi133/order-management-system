package com.ecommerce.service;

import com.ecommerce.model.Customer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomerService {
    private List<Customer> customers;

    public CustomerService(List<Customer> customers) {
        this.customers = customers;
    }

    // 1. Get all premium customers
    public List<Customer> getPremiumCustomers() {
        return customers.stream()
                .filter(Customer::isPremium)
                .collect(Collectors.toList());
    }

    // 2. Get customers from a specific city
    public List<Customer> getCustomersByCity(String city) {
        return customers.stream()
                .filter(c -> Objects.equals(c.getCity(), city))
                .collect(Collectors.toList());
    }

    // 3. Find customer by email (return Optional)
    public Optional<Customer> findByEmail(String email) {
        return customers.stream()
                .filter(c -> Objects.equals(c.getEmail(), email))
                .findFirst();
    }

    // 4. Get customer emails as a comma-separated string
    public String getEmailList() {
        return customers.stream()
                .map(Customer::getEmail)
                .collect(Collectors.joining(", "));
    }

    // 5. Get average total purchases of premium customers
    public double getAveragePremiumPurchases() {
        return customers.stream()
                .filter(Customer::isPremium)
                .mapToDouble(Customer::getTotalPurchases)
                .average()
                .orElse(0.0);
    }

    // 6. Get top N customers by total purchases
    public List<Customer> getTopCustomers(int n) {
        return customers.stream()
                .sorted(Comparator.comparingDouble(Customer::getTotalPurchases).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // 7. Count customers per city
    public Map<String, Long> getCustomerCountByCity() {
        return customers.stream()
                .collect(Collectors.groupingBy(Customer::getCity, Collectors.counting()));
    }

    // 8. Partition customers into premium and non-premium
    public Map<Boolean, List<Customer>> partitionByPremium() {
        return customers.stream()
                .collect(Collectors.partitioningBy(Customer::isPremium));
    }

    // 9. Transform customers using a provided function
    public <R> List<R> transformCustomers(Function<Customer, R> transformer) {
        return customers.stream()
                .map(transformer)
                .collect(Collectors.toList());
    }

    // 10. Process each customer with a provided consumer
    public void processCustomers(Consumer<Customer> processor) {
        customers.stream().forEach(processor);
    }
}

