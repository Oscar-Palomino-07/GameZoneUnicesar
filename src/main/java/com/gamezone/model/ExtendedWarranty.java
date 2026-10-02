package com.gamezone.model;

import java.time.LocalDate;

/**
 * Warranty type that extends the coverage of a product to twelve months at an
 * additional cost of ten percent of the product price.
 *
 * <p>It covers manufacturing defects as well as accidental damages suffered
 * during the extended period.</p>
 */
public class ExtendedWarranty extends Warranty {

    /**
     * Creates an extended warranty for the given product and sale.
     *
     * @param id        the unique warranty identifier
     * @param product   the product covered by the warranty
     * @param sale      the sale in which the product was purchased
     * @param startDate the date on which the coverage starts
     */
    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * @return the extended warranty coverage duration in months ({@code 12})
     */
    @Override
    public int getDurationInMonths() {
        return 12;
    }

    /**
     * @return the warranty type name {@code "Garantía Extendida"}
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Extendida";
    }

    /**
     * @return the additional cost of the extension, ten percent of the
     *         covered product price
     */
    @Override
    public double getAdditionalCost() {
        return getProduct().getPrice() * 0.10;
    }
}