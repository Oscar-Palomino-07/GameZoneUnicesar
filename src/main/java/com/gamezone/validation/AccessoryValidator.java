package com.gamezone.validation;

/**
 * Provides static validation methods for the accessory module.
 *
 * <p>This class belongs to the validation layer and verifies the data of a
 * controller, a cable or a memory before {@code AccessoryService} registers
 * it, without accessing persistence or services. It keeps the
 * {@link IllegalArgumentException} and the messages that the accessory module
 * has always shown to the user.</p>
 */
public final class AccessoryValidator {

    // Allowed values for the connection type of a controller.
    private static final String WIRELESS = "WIRELESS";
    private static final String WIRED = "WIRED";

    private AccessoryValidator() {
    }

    /**
     * Validates the attributes shared by every accessory.
     *
     * @param title the accessory name; must not be null or blank
     * @param price the unit price; must not be negative
     * @param stock the quantity in inventory; must not be negative
     * @throws IllegalArgumentException when the name is blank or the price or
     *         the stock are negative
     */
    public static void validateCommonData(String title, double price, int stock) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("El nombre del accesorio es obligatorio.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
    }

    /**
     * Converts the connection type of a controller to upper case and checks
     * that it is {@code WIRELESS} or {@code WIRED}.
     *
     * @param connectionType the connection type typed by the user
     * @return the connection type in upper case
     * @throws IllegalArgumentException when the value is not WIRELESS or WIRED
     */
    public static String normalizeConnectionType(String connectionType) {
        String value = connectionType == null ? "" : connectionType.trim().toUpperCase();
        if (!value.equals(WIRELESS) && !value.equals(WIRED)) {
            throw new IllegalArgumentException("El tipo de conexión debe ser WIRELESS o WIRED.");
        }
        return value;
    }

    /**
     * Validates the specific attributes of a cable.
     *
     * @param lengthInMeters the cable length; must be greater than zero
     * @param connectorType  the connector type; must not be null or blank
     * @throws IllegalArgumentException when the length is not positive or the
     *         connector type is blank
     */
    public static void validateCableData(double lengthInMeters, String connectorType) {
        if (lengthInMeters <= 0) {
            throw new IllegalArgumentException("La longitud del cable debe ser mayor que cero.");
        }
        if (isBlank(connectorType)) {
            throw new IllegalArgumentException("El tipo de conector es obligatorio.");
        }
    }

    /**
     * Validates the specific attributes of a memory.
     *
     * @param capacityInGb the storage capacity; must be greater than zero
     * @param memoryType   the memory type; must not be null or blank
     * @throws IllegalArgumentException when the capacity is not positive or
     *         the memory type is blank
     */
    public static void validateMemoryData(int capacityInGb, String memoryType) {
        if (capacityInGb <= 0) {
            throw new IllegalArgumentException("La capacidad de la memoria debe ser mayor que cero.");
        }
        if (isBlank(memoryType)) {
            throw new IllegalArgumentException("El tipo de memoria es obligatorio.");
        }
    }

    // Tells whether a text is null or only contains spaces.
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
