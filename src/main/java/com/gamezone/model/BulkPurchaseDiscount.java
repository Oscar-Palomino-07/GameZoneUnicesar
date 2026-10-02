package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * A promotion that grants a percentage discount over the total price of the
 * products in a sale only when the sale includes at least a minimum quantity
 * of products; otherwise it grants no discount.
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minimumQuantity;

    /**
     * Creates a bulk-purchase promotion with its common and particular
     * attributes.
     *
     * @param id                 the unique promotion identifier
     * @param name               the promotion name shown to the customer
     * @param startDate          the first date on which the promotion applies
     * @param endDate            the last date on which the promotion applies
     * @param discountPercentage the percentage discount granted (0-100)
     * @param minimumQuantity    the minimum number of products required in the
     *                           sale for the discount to apply
     */
    public BulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                                double discountPercentage, int minimumQuantity) {
        super(id, name, startDate, endDate, discountPercentage);
        this.minimumQuantity = minimumQuantity;
    }

    /**
     * @return the minimum number of products required for the discount to apply
     */
    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    /**
     * Updates the minimum quantity of products required for the discount.
     *
     * @param minimumQuantity the new minimum quantity
     */
    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    /**
     * Computes the discount as the percentage applied to the total price of
     * the given products, only when the product count reaches the minimum
     * quantity; otherwise the discount is zero.
     *
     * @param products the products considered for the discount; a {@code null}
     *                 list grants no discount
     * @return the discount amount in currency units
     */
    @Override
    public double calculateDiscount(List<Product> products) {
        if (products == null || products.size() < minimumQuantity) {
            return 0.0;
        }
        double subtotal = 0.0;
        for (Product product : products) {
            subtotal += product.getPrice();
        }
        return subtotal * getDiscountPercentage() / 100;
    }
}