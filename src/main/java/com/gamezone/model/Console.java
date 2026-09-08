package com.gamezone.model;

/**
 * Represents a video game console product sold by GameZone Unicesar.
 * Extends {@link Product} and adds the attributes particular to consoles:
 * brand, model and generation.
 */
public class Console extends Product {

    private String brand;
    private String model;
    private String generation;

    /**
     * Creates a console with its common and particular attributes.
     *
     * @param id         the unique product identifier
     * @param title      the console title or name
     * @param price      the unit price of the console
     * @param stock      the quantity available in inventory
     * @param brand      the console manufacturer brand
     * @param model      the console model
     * @param generation the console generation
     */
    public Console(String id, String title, double price, int stock,
                   String brand, String model, String generation) {
        super(id, title, price, stock);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    /**
     * @return the console manufacturer brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @return the console model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the console generation
     */
    public String getGeneration() {
        return generation;
    }

    /**
     * Builds a description that integrates the common attributes with the
     * attributes particular to consoles.
     *
     * @return a string describing the console
     */
    @Override
    public String getDescription() {
        return "Console: " + getTitle()
                + " | brand: " + brand
                + " | model: " + model
                + " | generation: " + generation
                + " | price: $" + getPrice()
                + " | stock: " + getStock();
    }
}