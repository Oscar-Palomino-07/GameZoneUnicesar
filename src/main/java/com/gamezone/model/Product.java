package com.gamezone.model;

/**
 * Abstract base class for every product sold by GameZone Unicesar.
 * Holds the attributes shared by all products: identifier, title, price and
 * available stock. Product cannot be instantiated directly; subclasses must
 * provide their own {@link #getDescription()} implementation.
 */
public abstract class Product {

    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Creates a product with the given common attributes.
     *
     * @param id    the unique product identifier
     * @param title the product title or name
     * @param price the unit price of the product
     * @param stock the quantity available in inventory
     */
    public Product(String id, String title, double price, int stock) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    /**
     * @return the unique product identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the product title or name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the unit price of the product
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return the quantity available in inventory
     */
    public int getStock() {
        return stock;
    }

    /**
     * Updates the available stock to the given quantity.
     *
     * @param quantity the new quantity available in inventory
     */
    public void updateStock(int quantity) {
        this.stock = quantity;
    }

    /**
     * Builds a complete textual description of the product, integrating the
     * attributes particular to the concrete product type.
     *
     * @return a string describing the product
     */
    public abstract String getDescription();
}