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
    // Subtotal and discount of the original sale, kept to refund what the customer actually paid.
    private double saleSubtotal;
    private double saleDiscount;

    /**
     * Creates a return of products from an original sale.
     *
     * @param id           the unique return identifier
     * @param saleId       the identifier of the original sale
     * @param customer     the customer who returns the products
     * @param seller       the seller of the original sale
     * @param products     the returned products, one entry per unit
     * @param saleSubtotal the subtotal of the original sale, before the discount
     * @param saleDiscount the discount granted to the original sale
     */
    public Return(String id, String saleId, Customer customer, Seller seller, List<Product> products,
                  double saleSubtotal, double saleDiscount) {
        this.id = id;
        this.saleId = saleId;
        this.date = LocalDate.now();
        this.customer = customer;
        this.seller = seller;
        this.products = products;
        this.saleSubtotal = saleSubtotal;
        this.saleDiscount = saleDiscount;
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

    /**
     * @return the subtotal of the original sale, before the discount
     */
    public double getSaleSubtotal() {
        return saleSubtotal;
    }

    /**
     * @return the discount granted to the original sale
     */
    public double getSaleDiscount() {
        return saleDiscount;
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