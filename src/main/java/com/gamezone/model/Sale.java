package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;

    public Sale(String id, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = LocalDate.now();
        this.customer = customer;
        this.seller = seller;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<Product> getProducts() {
        return products;
    }

    public double calculateTotal() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }
}