package com.gamezone.model;

import java.util.List;

/**
 * Represents a game controller sold by GameZone Unicesar.
 * Extends {@link Accessory} and adds the connection type of the controller
 * (for example, wireless or wired).
 */
public class Controller extends Accessory {

    private String connectionType;

    /**
     * Creates a controller with its common and particular attributes.
     *
     * @param id                   the unique product identifier
     * @param title                the controller title or name
     * @param price                the unit price of the controller
     * @param stock                the quantity available in inventory
     * @param compatibleConsoleIds the identifiers of the compatible consoles
     * @param connectionType       the connection type (wireless or wired)
     */
    public Controller(String id, String title, double price, int stock,
                      List<String> compatibleConsoleIds, String connectionType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.connectionType = connectionType;
    }

    /**
     * @return the connection type of the controller
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Updates the connection type of the controller.
     *
     * @param connectionType the new connection type
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Builds a description that integrates the common accessory attributes
     * with the connection type of the controller.
     *
     * @return a string describing the controller
     */
    @Override
    public String getDescription() {
        return "Controller: " + super.getDescription()
                + " | connection: " + connectionType;
    }
}