package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.service.AccessoryService;

import java.util.List;

/**
 * Submenu of the accessories module: registers controllers, cables and
 * memories and queries the accessories. Every option delegates to the
 * {@link AccessoryService}.
 */
public class AccessoryMenu implements Menu {

    private final AccessoryService accessoryService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the accessories menu.
     *
     * @param accessoryService the service used for accessory operations
     * @param input            the reader of the user input
     * @param errorReporter    the reporter that turns errors into messages
     */
    public AccessoryMenu(AccessoryService accessoryService, ConsoleInput input,
                         ConsoleErrorReporter errorReporter) {
        this.accessoryService = accessoryService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Accessories";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== ACCESSORIES ===");
            System.out.println("1. Register controller");
            System.out.println("2. Register cable");
            System.out.println("3. Register memory");
            System.out.println("4. List all accessories");
            System.out.println("5. List accessories by type");
            System.out.println("6. View accessories compatible with a console");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerController();
                    break;
                case 2:
                    registerCable();
                    break;
                case 3:
                    registerMemory();
                    break;
                case 4:
                    listAllAccessories();
                    break;
                case 5:
                    listAccessoriesByType();
                    break;
                case 6:
                    listAccessoriesCompatibleWith();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerController() {
        String title = input.readText("Title: ");
        double price = input.readDouble("Price: ");
        int stock = input.readInt("Stock: ");
        String connectionType = input.readText("Connection type (WIRELESS/WIRED): ");
        String consoleIdsInput = input.readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = input.parseIds(consoleIdsInput);
        errorReporter.runGuarded(() -> {
            Accessory controller = accessoryService.registerController(title, price, stock, connectionType, consoleIds);
            System.out.println("Controller " + controller.getId() + " registered.");
        });
    }

    private void registerCable() {
        String title = input.readText("Title: ");
        double price = input.readDouble("Price: ");
        int stock = input.readInt("Stock: ");
        double lengthInMeters = input.readDouble("Length in meters: ");
        String connectorType = input.readText("Connector type: ");
        String consoleIdsInput = input.readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = input.parseIds(consoleIdsInput);
        errorReporter.runGuarded(() -> {
            Accessory cable = accessoryService.registerCable(title, price, stock, lengthInMeters, connectorType, consoleIds);
            System.out.println("Cable " + cable.getId() + " registered.");
        });
    }

    private void registerMemory() {
        String title = input.readText("Title: ");
        double price = input.readDouble("Price: ");
        int stock = input.readInt("Stock: ");
        int capacityInGb = input.readInt("Capacity in GB: ");
        String memoryType = input.readText("Memory type: ");
        String consoleIdsInput = input.readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = input.parseIds(consoleIdsInput);
        errorReporter.runGuarded(() -> {
            Accessory memory = accessoryService.registerMemory(title, price, stock, capacityInGb, memoryType, consoleIds);
            System.out.println("Memory " + memory.getId() + " registered.");
        });
    }

    private void listAllAccessories() {
        List<Accessory> accessories = accessoryService.listAllAccessories();
        if (accessories.isEmpty()) {
            System.out.println("No accessories registered.");
            return;
        }
        for (Accessory accessory : accessories) {
            System.out.println(accessory.getDescription());
        }
    }

    private void listAccessoriesByType() {
        String type = input.readText("Type (CONTROLLER/CABLE/MEMORY): ");
        errorReporter.runGuarded(() -> {
            List<Accessory> accessories = accessoryService.listAccessoriesByType(type);
            if (accessories.isEmpty()) {
                System.out.println("No accessories of type " + type + " registered.");
                return;
            }
            for (Accessory accessory : accessories) {
                System.out.println(accessory.getDescription());
            }
        });
    }

    private void listAccessoriesCompatibleWith() {
        String consoleId = input.readText("Console id: ");
        errorReporter.runGuarded(() -> {
            List<Accessory> accessories = accessoryService.findAccessoriesCompatibleWith(consoleId);
            if (accessories.isEmpty()) {
                System.out.println("No accessories compatible with console " + consoleId + ".");
                return;
            }
            for (Accessory accessory : accessories) {
                System.out.println(accessory.getDescription());
            }
        });
    }
}
