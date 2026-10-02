package com.gamezone.model;

import java.time.LocalDate;

/**
 * Warranty type that covers a product for six months at no additional cost.
 *
 * <p>It is the standard guarantee included with every sale and covers
 * manufacturing defects only.</p>
 */
public class BasicWarranty extends Warranty {

    /**
     * Creates a basic warranty of six months for the given product and sale.
     *
     * @param id        the unique warranty identifier
     * @param product   the product covered by the warranty
     * @param sale      the sale in which the product was purchased
     * @param startDate the date on which the coverage starts
     */
    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * @return the basic warranty coverage duration in months ({@code 6})
     */
    @Override
    public int getDurationInMonths() {
        return 6;
    }

    /**
     * @return the warranty type name {@code "Garantía Básica"}
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Básica";
    }

    /**
     * @return the additional cost of the basic warranty ({@code 0.0})
     */
    @Override
    public double getAdditionalCost() {
        return 0.0;
    }
}