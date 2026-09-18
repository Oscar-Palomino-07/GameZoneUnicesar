package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for every accessory sold by GameZone Unicesar
 * (controllers, cables and memories).
 *
 * <p>Extends {@link Product} to reuse the attributes and behavior shared by
 * every sellable item (id, title, price and stock), and adds the list of
 * consoles the accessory is compatible with. Compatibility is stored as
 * console identifiers instead of {@link Console} objects, so the accessory
 * does not duplicate console data and can be persisted as plain values.</p>
 */
public abstract class Accessory extends Product {

    private List<String> compatibleConsoleIds;

    /**
     * Creates an accessory with its common attributes and the identifiers of
     * the consoles it is compatible with.
     *
     * @param id                   the unique product identifier
     * @param title                the accessory title or name
     * @param price                the unit price of the accessory
     * @param stock                the quantity available in inventory
     * @param compatibleConsoleIds the identifiers of the compatible consoles;
     *                             {@code null} is treated as an empty list
     */
    public Accessory(String id, String title, double price, int stock, List<String> compatibleConsoleIds) {
        super(id, title, price, stock);
        setCompatibleConsoleIds(compatibleConsoleIds);
    }

    /**
     * @return the identifiers of the consoles this accessory is compatible with
     */
    public List<String> getCompatibleConsoleIds() {
        return compatibleConsoleIds;
    }

    /**
     * Replaces the list of compatible console identifiers.
     *
     * @param compatibleConsoleIds the new list of console identifiers;
     *                             {@code null} is treated as an empty list
     */
    public void setCompatibleConsoleIds(List<String> compatibleConsoleIds) {
        this.compatibleConsoleIds = compatibleConsoleIds == null
                ? new ArrayList<>()
                : new ArrayList<>(compatibleConsoleIds);
    }

    /**
     * Registers a console as compatible with this accessory, ignoring
     * duplicated identifiers.
     *
     * @param consoleId the identifier of the console to add
     */
    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

    /**
     * Removes a console from the compatibility list, if present.
     *
     * @param consoleId the identifier of the console to remove
     */
    public void removeCompatibleConsole(String consoleId) {
        compatibleConsoleIds.remove(consoleId);
    }

    /**
     * Tells whether this accessory is compatible with the given console.
     *
     * @param consoleId the identifier of the console to check
     * @return {@code true} when the console is in the compatibility list
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }

    /**
     * Builds the part of the description shared by every accessory. Concrete
     * accessories should override this method, prefix their type and append
     * their particular attributes to this text.
     *
     * @return a string with the common accessory information
     */
    @Override
    public String getDescription() {
        String consoles = compatibleConsoleIds.isEmpty()
                ? "none"
                : String.join(", ", compatibleConsoleIds);
        return getTitle()
                + " | price: $" + getPrice()
                + " | stock: " + getStock()
                + " | compatible consoles: " + consoles;
    }
}