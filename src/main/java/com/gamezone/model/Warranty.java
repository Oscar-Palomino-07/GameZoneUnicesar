package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class for every warranty granted by GameZone Unicesar over a
 * sold product.
 *
 * <p>A warranty is always tied to a product and to the sale in which that
 * product was purchased. Its coverage starts on the sale date and ends on a
 * date calculated automatically from the concrete warranty type (six months
 * for the basic warranty, twelve for the extended one). Because the coverage
 * period differs per type, the expiration date is derived inside the
 * constructor from the abstract {@link #getDurationInMonths()} method, so the
 * subclasses only need to declare their duration.</p>
 */
public abstract class Warranty {

    private String id;
    private Product product;
    private Sale sale;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a warranty for the given product and sale, computing the
     * expiration date from the duration supplied by the concrete warranty
     * type.
     *
     * @param id        the unique warranty identifier
     * @param product   the product covered by the warranty
     * @param sale      the sale in which the product was purchased
     * @param startDate the date on which the coverage starts (the sale date)
     */
    public Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(getDurationInMonths());
    }

    /**
     * @return the unique warranty identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the product covered by the warranty
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @return the sale in which the covered product was purchased
     */
    public Sale getSale() {
        return sale;
    }

    /**
     * @return the date on which the coverage starts
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the date on which the coverage expires
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Returns the duration of the coverage in months, defined by each
     * concrete warranty type.
     *
     * @return the coverage duration in months
     */
    public abstract int getDurationInMonths();

    /**
     * Returns the user-facing name of the warranty type.
     *
     * @return the warranty type name
     */
    public abstract String getWarrantyType();

    /**
     * Returns the additional cost that the warranty adds to its sale.
     *
     * @return the additional cost in currency units
     */
    public abstract double getAdditionalCost();

    /**
     * Tells whether the warranty covers the given date, that is, whether the
     * date falls within the inclusive start-end window.
     *
     * @param date the date to check
     * @return {@code true} when the date is within the coverage window and is
     *         not {@code null}
     */
    public boolean isActive(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Builds a formatted warranty certificate in Spanish with the detail of
     * the warranty: identifiers, covered product, sale, coverage period,
     * additional cost and current validity status.
     *
     * @return the formatted warranty certificate
     */
    public String generateWarrantyCertificate() {
        StringBuilder certificate = new StringBuilder();
        certificate.append("Certificado de garantia\n");
        certificate.append("========================\n");
        certificate.append("Identificador: ").append(id).append("\n");
        certificate.append("Tipo: ").append(getWarrantyType()).append("\n");
        certificate.append("Producto: ").append(product.getTitle()).append("\n");
        certificate.append("Precio: $").append(product.getPrice()).append("\n");
        certificate.append("Venta: ").append(sale.getId()).append("\n");
        certificate.append("Duracion: ").append(getDurationInMonths()).append(" meses\n");
        certificate.append("Inicio: ").append(startDate).append("\n");
        certificate.append("Vencimiento: ").append(endDate).append("\n");
        certificate.append("Costo adicional: $").append(getAdditionalCost()).append("\n");
        certificate.append("Estado: ").append(isActive(LocalDate.now()) ? "Vigente" : "Expirada");
        return certificate.toString();
    }
}