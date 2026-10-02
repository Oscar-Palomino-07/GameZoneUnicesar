package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
    // Refundable cost of the warranties cancelled because their console was returned.
    private double warrantyRefund;

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

    /**
     * @return the amount refunded for the warranties cancelled by this
     *         return, zero when no extended warranty was cancelled
     */
    public double getWarrantyRefund() {
        return warrantyRefund;
    }

    /**
     * Sets the amount refunded for the warranties cancelled by this return.
     * Only extended warranties have a cost, so only they add to this value.
     *
     * @param warrantyRefund the refundable cost of the cancelled warranties,
     *                       never negative
     */
    public void setWarrantyRefund(double warrantyRefund) {
        this.warrantyRefund = warrantyRefund;
    }

    /**
     * Calculates the amount refunded to the customer. Each returned product is
     * refunded proportionally to the discount of the original sale:
     * {@code price * (1 - saleDiscount / saleSubtotal)}, so the customer gets
     * back what was actually paid and not the list price. The cost of the
     * extended warranties cancelled because their console was returned is
     * added in full, since the sale discount never applied to it.
     *
     * @return the total refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice() - calculateProportionalDiscount(product);
        }
        return total + warrantyRefund;
    }

    // Share of the sale discount that corresponds to the product; zero when the sale had no discount.
    private double calculateProportionalDiscount(Product product) {
        if (saleSubtotal <= 0 || saleDiscount <= 0) {
            return 0.0;
        }
        return product.getPrice() * saleDiscount / saleSubtotal;
    }

    /**
     * Builds the return receipt in Spanish. For every returned product it shows
     * the list price, the proportional share of the sale discount and the
     * refunded amount, then the refund of the cancelled warranties and the
     * total refund.
     *
     * @return the formatted return receipt
     */
    public String generateReturnReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Recibo de devolución\n");
        receipt.append("====================\n");
        receipt.append("ID: ").append(id).append("\n");
        receipt.append("Venta: ").append(saleId).append("\n");
        receipt.append("Fecha: ").append(date).append("\n");
        receipt.append("Cliente: ").append(customer.getId()).append("\n");
        receipt.append("Vendedor: ").append(seller.getId()).append("\n");
        receipt.append("Productos:\n");
        for (Product product : products) {
            double discount = calculateProportionalDiscount(product);
            receipt.append("  - ").append(product.getTitle()).append(" (").append(product.getId()).append(")\n");
            receipt.append("      Precio de lista: $").append(formatAmount(product.getPrice())).append("\n");
            receipt.append("      Descuento proporcional: -$").append(formatAmount(discount)).append("\n");
            receipt.append("      Reembolso: $").append(formatAmount(product.getPrice() - discount)).append("\n");
        }
        receipt.append("Garantías anuladas (reembolso): $").append(formatAmount(warrantyRefund)).append("\n");
        receipt.append("Total reembolsado: $").append(formatAmount(calculateRefundAmount())).append("\n");
        return receipt.toString();
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

}