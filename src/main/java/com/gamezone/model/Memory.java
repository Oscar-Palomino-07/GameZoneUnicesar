package com.gamezone.model;

import java.util.List;

/**
 * Represents a memory unit sold by GameZone Unicesar.
 * Extends {@link Accessory} and adds the storage capacity in gigabytes and
 * the memory type (for example, SD, microSD or internal card).
 */
public class Memory extends Accessory {

    private int capacityInGb;
    private String memoryType;

    /**
     * Creates a memory with its common and particular attributes.
     *
     * @param id                   the unique product identifier
     * @param title                the memory title or name
     * @param price                the unit price of the memory
     * @param stock                the quantity available in inventory
     * @param compatibleConsoleIds the identifiers of the compatible consoles
     * @param capacityInGb         the storage capacity in gigabytes
     * @param memoryType           the memory type
     */
    public Memory(String id, String title, double price, int stock,
                  List<String> compatibleConsoleIds, int capacityInGb, String memoryType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.capacityInGb = capacityInGb;
        this.memoryType = memoryType;
    }

    /**
     * @return the storage capacity in gigabytes
     */
    public int getCapacityInGb() {
        return capacityInGb;
    }

    /**
     * Updates the storage capacity of the memory.
     *
     * @param capacityInGb the new capacity in gigabytes
     */
    public void setCapacityInGb(int capacityInGb) {
        this.capacityInGb = capacityInGb;
    }

    /**
     * @return the memory type
     */
    public String getMemoryType() {
        return memoryType;
    }

    /**
     * Updates the memory type.
     *
     * @param memoryType the new memory type
     */
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * Builds a description that integrates the common accessory attributes
     * with the capacity and type of the memory.
     *
     * @return a string describing the memory
     */
    @Override
    public String getDescription() {
        return "Memory: " + super.getDescription()
                + " | capacity: " + capacityInGb + " GB"
                + " | type: " + memoryType;
    }
}