package com.gamezone.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
 * <p>The subtotal of the sale is the sum of the prices of the included
 * products. The final total, returned by {@link #calculateTotal()}, is the
 * subtotal minus the discount of the applied promotion plus the extra cost
 * of the extended warranties.</p>
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;
    private double warrantyExtraCost = 0.0;
    private String appliedPromotionName;
    private double discountAmount;

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
     * @return the extra cost added to the sale by extended warranties
     */
    public double getWarrantyExtraCost() {
        return warrantyExtraCost;
    }

    /**
     * Sets the extra cost that extended warranties add to the sale.
     *
     * @param warrantyExtraCost the new extra cost of the extended warranties
     */
    public void setWarrantyExtraCost(double warrantyExtraCost) {
        this.warrantyExtraCost = warrantyExtraCost;
    }

    /**
     * @return the name of the promotion applied to the sale, or {@code null}
     *         when no promotion was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * Sets the name of the promotion applied to the sale.
     *
     * @param appliedPromotionName the promotion name, or {@code null} when no
     *                             promotion was applied
     */
    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    /**
     * @return the monetary discount granted by the applied promotion, zero
     *         when no promotion was applied
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the monetary discount granted by the applied promotion.
     *
     * @param discountAmount the discount amount, never negative
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Calculates the subtotal of the sale, that is, the sum of the price of
     * every included product before any discount or warranty cost.
     *
     * @return the subtotal of the sale
     */
    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (Product product : products) {
            subtotal += product.getPrice();
        }
        return subtotal;
    }

    /**
     * Calculates the final total of the sale: the subtotal minus the discount
     * of the applied promotion plus the extra cost of the extended
     * warranties.
     *
     * @return the final total of the sale
     */
    public double calculateTotal() {
        return calculateSubtotal() - discountAmount + warrantyExtraCost;
    }

    public boolean canBeReturned() {
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(this.date, today);

        return daysBetween >= 0 && daysBetween <= 30;
    }

}