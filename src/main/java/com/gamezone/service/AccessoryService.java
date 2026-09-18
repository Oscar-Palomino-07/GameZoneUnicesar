package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

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

    // Accessory types accepted by listAccessoriesByType.
    private static final String CONTROLLER_TYPE = "CONTROLLER";
    private static final String CABLE_TYPE = "CABLE";
    private static final String MEMORY_TYPE = "MEMORY";

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

    /**
     * Returns all registered accessories.
     *
     * @return an unmodifiable view of all registered accessories
     */
    public List<Accessory> listAllAccessories() {
        // Read-only view: callers cannot add or remove accessories without this service.
        return Collections.unmodifiableList(accessories);
    }

    /**
     * Returns the accessories of the given type.
     *
     * @param type CONTROLLER, CABLE or MEMORY, case insensitive
     * @return the accessories of that type, or an empty list when none match
     * @throws IllegalArgumentException when the type is blank or not recognized
     */
    public List<Accessory> listAccessoriesByType(String type) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("Debe indicar el tipo de accesorio.");
        }
        String normalizedType = type.trim().toUpperCase();
        if (!normalizedType.equals(CONTROLLER_TYPE) && !normalizedType.equals(CABLE_TYPE)
                && !normalizedType.equals(MEMORY_TYPE)) {
            throw new IllegalArgumentException("Tipo de accesorio no válido: " + type
                    + ". Use CONTROLLER, CABLE o MEMORY.");
        }
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (matchesType(accessory, normalizedType)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Returns the accessories that are compatible with the given console.
     *
     * @param consoleId the identifier of the console
     * @return the compatible accessories, or an empty list when none match
     * @throws IllegalArgumentException when the console identifier is blank
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        if (isBlank(consoleId)) {
            throw new IllegalArgumentException("Debe indicar el identificador de la consola.");
        }
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            // The compatibility rule belongs to the model; the service only filters.
            if (accessory.isCompatibleWith(consoleId.trim())) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Finds an accessory by its identifier.
     *
     * @param id the identifier to look for
     * @return the matching accessory, or {@code null} if it is not registered
     */
    public Accessory findById(String id) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equals(id)) {
                return accessory;
            }
        }
        return null;
    }

    /**
     * Sets the stock of the given accessory to the given quantity and persists
     * the change. As in ProductService, the quantity is the new stock value,
     * not an amount to add or subtract.
     *
     * @param accessoryId the identifier of the accessory to update
     * @param quantity    the new quantity available in inventory
     * @throws IllegalArgumentException when the accessory does not exist or the
     *         quantity is negative
     */
    public void updateStock(String accessoryId, int quantity) {
        Accessory accessory = findById(accessoryId);
        if (accessory == null) {
            throw new IllegalArgumentException("Accesorio no encontrado: " + accessoryId);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        // updateStock is inherited from Product.
        accessory.updateStock(quantity);
        save();
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

    // Tells whether an accessory belongs to the given type using its real class.
    private boolean matchesType(Accessory accessory, String type) {
        switch (type) {
            case CONTROLLER_TYPE:
                return accessory instanceof Controller;
            case CABLE_TYPE:
                return accessory instanceof Cable;
            case MEMORY_TYPE:
                return accessory instanceof Memory;
            default:
                return false;
        }
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