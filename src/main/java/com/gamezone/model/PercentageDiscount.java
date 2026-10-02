package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * A promotion that grants a percentage discount over the total price of the
 * products in a sale, regardless of their category.
 */
public class PercentageDiscount extends Promotion {

    /**
     * Creates a percentage promotion with its common attributes.
     *
     * @param id                 the unique promotion identifier
     * @param name               the promotion name shown to the customer
     * @param startDate          the first date on which the promotion applies
     * @param endDate            the last date on which the promotion applies
     * @param discountPercentage the percentage discount granted (0-100)
     */
    public PercentageDiscount(String id, String name, LocalDate startDate,
                              LocalDate endDate, double discountPercentage) {
        super(id, name, startDate, endDate, discountPercentage);
    }

    /**
     * Computes the discount as the percentage applied to the total price of
     * the given products.
     *
     * @param products the products considered for the discount; a {@code null}
     *                 list grants no discount
     * @return the discount amount in currency units
     */
    @Override
    public double calculateDiscount(List<Product> products) {
        if (products == null) {
            return 0.0;
        }
        double subtotal = 0.0;
        for (Product product : products) {
            subtotal += product.getPrice();
        }
        return subtotal * getDiscountPercentage() / 100;
    }
}