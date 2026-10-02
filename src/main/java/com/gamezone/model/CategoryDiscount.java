package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * A promotion that grants a percentage discount only over the products of a
 * specific category. The target category is compared, ignoring the case,
 * against the value returned by {@link Product#getCategory()} (for example,
 * {@code VIDEOGAME} or {@code CONSOLE}).
 */
public class CategoryDiscount extends Promotion {

    private String targetCategory;

    /**
     * Creates a category promotion with its common and particular attributes.
     *
     * @param id                 the unique promotion identifier
     * @param name               the promotion name shown to the customer
     * @param startDate          the first date on which the promotion applies
     * @param endDate            the last date on which the promotion applies
     * @param discountPercentage the percentage discount granted (0-100)
     * @param targetCategory     the category the discount applies to; compared
     *                           ignoring the case against {@code getCategory()}
     */
    public CategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                            double discountPercentage, String targetCategory) {
        super(id, name, startDate, endDate, discountPercentage);
        this.targetCategory = targetCategory;
    }

    /**
     * @return the category the discount applies to
     */
    public String getTargetCategory() {
        return targetCategory;
    }

    /**
     * Updates the target category of the promotion.
     *
     * @param targetCategory the new target category
     */
    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    /**
     * Computes the discount as the percentage applied only to the prices of
     * the products whose category matches the target category.
     *
     * @param products the products considered for the discount; a {@code null}
     *                 list grants no discount
     * @return the discount amount in currency units
     */
    @Override
    public double calculateDiscount(List<Product> products) {
        if (products == null || targetCategory == null) {
            return 0.0;
        }
        double categoryTotal = 0.0;
        for (Product product : products) {
            if (targetCategory.equalsIgnoreCase(product.getCategory())) {
                categoryTotal += product.getPrice();
            }
        }
        return categoryTotal * getDiscountPercentage() / 100;
    }
}