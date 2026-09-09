package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a sale registered at GameZone Unicesar.
 *
 * <p>A sale ties a customer, the seller who attended them and the list of
 * purchased products. The date of the sale is captured automatically at the
 * moment the sale is constructed, so it always reflects the real time in
 * which the sale was registered and cannot be accidentally omitted by the
 * caller.</p>
 *
 * <p>The total amount of the sale is derived from the prices of the included
 * products through {@link #calculateTotal()}.</p>
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;

    /**
     * Creates a sale with the given data, capturing the current date
     * automatically.
     *
     * @param id       the identifier of the sale, assigned by the sale service
     * @param customer the customer who made the purchase
     * @param seller   the seller who attended the customer
     * @param products the products included in the sale
     */
    public Sale(String id, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = LocalDate.now();
        this.customer = customer;
        this.seller = seller;
        this.products = products;
    }

    /**
     * @return the identifier of the sale
     */
    public String getId() {
        return id;
    }

    /**
     * @return the date on which the sale was registered
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the customer who made the purchase
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @return the seller who attended the customer
     */
    public Seller getSeller() {
        return seller;
    }

    /**
     * @return the products included in the sale
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * Calculates the total amount of the sale by adding the price of every
     * included product.
     *
     * @return the total amount of the sale
     */
    public double calculateTotal() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }
}