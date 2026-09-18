package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the business operations for the accessory module of GameZone
 * Unicesar. Accessory identifiers are generated automatically following the
 * A-N sequence, and every change is persisted immediately through the
 * {@link AccessoryRepository}.
 */
public class AccessoryService {

    // Prefix of the accessory identifiers: A-1, A-2, A-3...
    private static final String ID_PREFIX = "A-";

    // Allowed values for the connection type of a controller.
    private static final String WIRELESS = "WIRELESS";
    private static final String WIRED = "WIRED";

    private final AccessoryRepository repository;
    // In-memory list of accessories, loaded once when the service is created.
    private final List<Accessory> accessories;

    /**
     * Creates the accessory service and loads the stored accessories.
     *
     * @param repository the repository used to persist and load accessories
     */
    public AccessoryService(AccessoryRepository repository) {
        this.repository = repository;
        this.accessories = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers a new controller.
     *
     * @param title                the controller name
     * @param price                the unit price
     * @param stock                the quantity available in inventory
     * @param connectionType       WIRELESS or WIRED
     * @param compatibleConsoleIds identifiers of the compatible consoles
     * @return the registered controller
     * @throws IllegalArgumentException when any value is invalid
     */
    public Controller registerController(String title, double price, int stock,
                                         String connectionType, List<String> compatibleConsoleIds) {
        validateCommonFields(title, price, stock);
        String connection = normalizeConnectionType(connectionType);

        Controller controller = new Controller(nextAccessoryId(), title.trim(), price, stock,
                connection, cleanConsoleIds(compatibleConsoleIds));
        accessories.add(controller);
        save();
        // Returned so the menu can show the generated identifier.
        return controller;
    }

    /**
     * Registers a new cable.
     *
     * @param title                the cable name
     * @param price                the unit price
     * @param stock                the quantity available in inventory
     * @param lengthInMeters       the cable length in meters, greater than zero
     * @param connectorType        the connector type, for example HDMI or USB
     * @param compatibleConsoleIds identifiers of the compatible consoles
     * @return the registered cable
     * @throws IllegalArgumentException when any value is invalid
     */
    public Cable registerCable(String title, double price, int stock, double lengthInMeters,
                               String connectorType, List<String> compatibleConsoleIds) {
        validateCommonFields(title, price, stock);
        if (lengthInMeters <= 0) {
            throw new IllegalArgumentException("La longitud del cable debe ser mayor que cero.");
        }
        if (isBlank(connectorType)) {
            throw new IllegalArgumentException("El tipo de conector es obligatorio.");
        }
        Cable cable = new Cable(nextAccessoryId(), title.trim(), price, stock, lengthInMeters,
                connectorType.trim(), cleanConsoleIds(compatibleConsoleIds));
        accessories.add(cable);
        save();
        return cable;
    }

    /**
     * Registers a new memory.
     *
     * @param title                the memory name
     * @param price                the unit price
     * @param stock                the quantity available in inventory
     * @param capacityInGb         the storage capacity in gigabytes, greater than zero
     * @param memoryType           the memory type, for example SD, microSD or INTERNAL
     * @param compatibleConsoleIds identifiers of the compatible consoles
     * @return the registered memory
     * @throws IllegalArgumentException when any value is invalid
     */
    public Memory registerMemory(String title, double price, int stock, int capacityInGb,
                                 String memoryType, List<String> compatibleConsoleIds) {
        validateCommonFields(title, price, stock);
        if (capacityInGb <= 0) {
            throw new IllegalArgumentException("La capacidad de la memoria debe ser mayor que cero.");
        }
        if (isBlank(memoryType)) {
            throw new IllegalArgumentException("El tipo de memoria es obligatorio.");
        }
        Memory memory = new Memory(nextAccessoryId(), title.trim(), price, stock, capacityInGb,
                memoryType.trim(), cleanConsoleIds(compatibleConsoleIds));
        accessories.add(memory);
        save();
        return memory;
    }

    // Validates the attributes shared by every accessory.
    private void validateCommonFields(String title, double price, int stock) {
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

    // Converts the connection type to upper case and checks it is WIRELESS or WIRED.
    private String normalizeConnectionType(String connectionType) {
        String value = connectionType == null ? "" : connectionType.trim().toUpperCase();
        if (!value.equals(WIRELESS) && !value.equals(WIRED)) {
            throw new IllegalArgumentException("El tipo de conexión debe ser WIRELESS o WIRED.");
        }
        return value;
    }

    // Removes blank and repeated console identifiers, keeping the original order.
    private List<String> cleanConsoleIds(List<String> consoleIds) {
        List<String> cleaned = new ArrayList<>();
        if (consoleIds == null) {
            return cleaned;
        }
        for (String consoleId : consoleIds) {
            if (!isBlank(consoleId) && !cleaned.contains(consoleId.trim())) {
                cleaned.add(consoleId.trim());
            }
        }
        return cleaned;
    }

    // Returns the next identifier of the A-N sequence, based on the highest existing number.
    private String nextAccessoryId() {
        int max = 0;
        for (Accessory accessory : accessories) {
            String id = accessory.getId();
            if (id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // An identifier without a number does not change the sequence.
                }
            }
        }
        return ID_PREFIX + (max + 1);
    }

    // Tells whether a text is null or only contains spaces.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Persists the current list of accessories.
    private void save() {
        repository.saveAll(accessories);
    }
}