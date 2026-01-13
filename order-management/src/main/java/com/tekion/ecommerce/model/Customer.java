package com.ecommerce.model;

public class Customer {
    private Long id;
    private String name;
    private String email;
    private String city;
    private boolean premium;
    private double totalPurchases;

    public Customer(Long id, String name, String email, String city,
                    boolean premium, double totalPurchases) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.premium = premium;
        this.totalPurchases = totalPurchases;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public boolean isPremium() { return premium; }
    public double getTotalPurchases() { return totalPurchases; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', premium=" + premium + "}";
    }
}

