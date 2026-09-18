package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstract base class for every promotion run by GameZone Unicesar.
 *
 * <p>Represents a marketing campaign that grants a monetary discount over a
 * set of products during a given validity window. Every promotion knows the
 * period in which it applies and the percentage discount it grants; each
 * concrete promotion type decides how that percentage is turned into money
 * through {@link #calculateDiscount(List)}.</p>
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double discountPercentage;

    /**
     * Creates a promotion with its common attributes.
     *
     * @param id                 the unique promotion identifier
     * @param name               the promotion name shown to the customer
     * @param startDate          the first date on which the promotion applies
     * @param endDate            the last date on which the promotion applies
     * @param discountPercentage the percentage discount granted (0-100)
     */
    public Promotion(String id, String name, LocalDate startDate, LocalDate endDate, double discountPercentage) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPercentage = discountPercentage;
    }

    /**
     * @return the unique promotion identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the promotion identifier.
     *
     * @param id the new identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the promotion name shown to the customer
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the promotion name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the first date on which the promotion applies
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Updates the start date of the promotion.
     *
     * @param startDate the new start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the last date on which the promotion applies
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Updates the end date of the promotion.
     *
     * @param endDate the new end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the percentage discount granted by the promotion
     */
    public double getDiscountPercentage() {
        return discountPercentage;
    }

    /**
     * Updates the percentage discount granted by the promotion.
     *
     * @param discountPercentage the new percentage discount (0-100)
     */
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    /**
     * Tells whether the promotion applies on the given date, that is, whether
     * the date falls within the inclusive start-end window.
     *
     * @param date the date to check
     * @return {@code true} when the date is within the validity window and is
     *         not {@code null}
     */
    public boolean isActive(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Tells whether the promotion is currently active, using today's date.
     *
     * @return {@code true} when today falls within the validity window
     */
    public boolean isActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Computes the monetary discount that this promotion would grant over the
     * given set of products. Each concrete promotion type decides how the
     * percentage discount is applied.
     *
     * @param products the products considered for the discount; a {@code null}
     *                 list grants no discount
     * @return the discount amount in currency units
     */
    public abstract double calculateDiscount(List<Product> products);
}