package com.gamezone.model;

import java.util.List;

/**
 * Represents a cable sold by GameZone Unicesar.
 * Extends {@link Accessory} and adds the length of the cable in meters and
 * its connector type (for example, HDMI, USB or optical).
 */
public class Cable extends Accessory {

    private double lengthInMeters;
    private String connectorType;

    /**
     * Creates a cable with its common and particular attributes.
     *
     * @param id                   the unique product identifier
     * @param title                the cable title or name
     * @param price                the unit price of the cable
     * @param stock                the quantity available in inventory
     * @param compatibleConsoleIds the identifiers of the compatible consoles
     * @param lengthInMeters       the length of the cable in meters
     * @param connectorType        the connector type of the cable
     */
    public Cable(String id, String title, double price, int stock,
                 List<String> compatibleConsoleIds, double lengthInMeters, String connectorType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.lengthInMeters = lengthInMeters;
        this.connectorType = connectorType;
    }

    /**
     * @return the length of the cable in meters
     */
    public double getLengthInMeters() {
        return lengthInMeters;
    }

    /**
     * Updates the length of the cable.
     *
     * @param lengthInMeters the new length in meters
     */
    public void setLengthInMeters(double lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    /**
     * @return the connector type of the cable
     */
    public String getConnectorType() {
        return connectorType;
    }

    /**
     * Updates the connector type of the cable.
     *
     * @param connectorType the new connector type
     */
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    /**
     * Builds a description that integrates the common accessory attributes
     * with the length and connector type of the cable.
     *
     * @return a string describing the cable
     */
    @Override
    public String getDescription() {
        return "Cable: " + super.getDescription()
                + " | length: " + lengthInMeters + " m"
                + " | connector: " + connectorType;
    }
}