package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

public class Return {

    private String id;
    private String saleId;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;

    public Return(String id, String saleId, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.saleId = saleId;
        this.date = LocalDate.now();
        this.customer = customer;
        this.seller = seller;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public String getSaleId() {
        return saleId;
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

    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }

    public String generateReturnReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Return Receipt\n");
        receipt.append("================\n");
        receipt.append("ID: ").append(id).append("\n");
        receipt.append("Sale: ").append(saleId).append("\n");
        receipt.append("Date: ").append(date).append("\n");
        receipt.append("Customer: ").append(customer.getId()).append("\n");
        receipt.append("Seller: ").append(seller.getId()).append("\n");
        receipt.append("Products:\n");
        for (Product product : products) {
            receipt.append("- ").append(product.getTitle()).append(" ").append(product.getPrice()).append("\n");
        }
        receipt.append("Total: ").append(calculateRefundAmount()).append("\n");
        return receipt.toString();
    }

}