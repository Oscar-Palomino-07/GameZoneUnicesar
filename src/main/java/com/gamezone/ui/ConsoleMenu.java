package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console-based user interface of GameZone Unicesar.
 *
 * <p>Shows a main menu that gives access to three submenus, one for each
 * module of the application: products, people and sales. Every user option
 * delegates to the corresponding service, so the user interface never
 * accesses the persistence layer directly.</p>
 */
public class ConsoleMenu {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
    private final ReturnService returnService;
    private final AccessoryService accessoryService;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates the console menu with all five services of the application.
     *
     * @param productService  the service used for product operations
     * @param personService   the service used for person operations
     * @param saleService     the service used for sale operations
     * @param returnService   the service used for return operations
     * @param accessoryService the service used for accessory operations
     */
    public ConsoleMenu(ProductService productService, PersonService personService,
                       SaleService saleService, ReturnService returnService, AccessoryService accessoryService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.returnService = returnService;
        this.accessoryService = accessoryService;
    }

    /**
     * Runs the main loop of the application until the user chooses to exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("=== GAMEZONE UNICESAR ===");
            System.out.println("1. Products");
            System.out.println("2. People");
            System.out.println("3. Sales");
            System.out.println("4. Returns");
            System.out.println("5. Accessories");
            System.out.println("6. Reports");
            System.out.println("0. Exit");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    productMenu();
                    break;
                case 2:
                    personMenu();
                    break;
                case 3:
                    saleMenu();
                    break;
                case 4:
                    returnMenu();
                    break;
                case 5:
                    accessoryMenu();
                    break;
                case 6:
                    reportsMenu();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void productMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== PRODUCTS ===");
            System.out.println("1. Register video game");
            System.out.println("2. Register console");
            System.out.println("3. List all products");
            System.out.println("0. Back");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerVideoGame();
                    break;
                case 2:
                    registerConsole();
                    break;
                case 3:
                    listAllProducts();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void personMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== PEOPLE ===");
            System.out.println("1. Register customer");
            System.out.println("2. List customers");
            System.out.println("3. List sellers");
            System.out.println("0. Back");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    listCustomers();
                    break;
                case 3:
                    listSellers();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void saleMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== SALES ===");
            System.out.println("1. Register sale");
            System.out.println("2. View all sales");
            System.out.println("3. View sales by customer");
            System.out.println("4. View sales by seller");
            System.out.println("0. Back");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerSale();
                    break;
                case 2:
                    viewAllSales();
                    break;
                case 3:
                    viewSalesByCustomer();
                    break;
                case 4:
                    viewSalesBySeller();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerVideoGame() {
        String id = readText("Product id: ");
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        String platform = readText("Platform: ");
        String genre = readText("Genre: ");
        String ageRating = readText("Age rating: ");
        try {
            productService.registerVideoGame(id, title, price, stock, platform, genre, ageRating);
            System.out.println(productService.findById(id) != null
                    ? "Video game registered."
                    : "Video game could not be registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void registerConsole() {
        String id = readText("Product id: ");
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        String brand = readText("Brand: ");
        String model = readText("Model: ");
        String generation = readText("Generation: ");
        try {
            productService.registerConsole(id, title, price, stock, brand, model, generation);
            System.out.println(productService.findById(id) != null
                    ? "Console registered."
                    : "Console could not be registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listAllProducts() {
        List<Product> products = productService.listAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products registered.");
            return;
        }
        for (Product product : products) {
            System.out.println(product.getDescription());
        }
    }

    private void registerCustomer() {
        String id = readText("Customer id: ");
        String firstName = readText("First name: ");
        String lastName = readText("Last name: ");
        String phone = readText("Phone: ");
        String email = readText("Email: ");
        try {
            personService.registerCustomer(id, firstName, lastName, phone, email);
            System.out.println("Customer registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listCustomers() {
        List<Customer> customers = personService.listAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        for (Customer customer : customers) {
            System.out.println(customer.getId() + " | " + customer.getFirstName() + " " + customer.getLastName()
                    + " | " + customer.getPhone() + " | " + customer.getEmail());
        }
    }

    private void listSellers() {
        List<Seller> sellers = personService.listAllSellers();
        if (sellers.isEmpty()) {
            System.out.println("No sellers registered.");
            return;
        }
        for (Seller seller : sellers) {
            System.out.println(seller.getId() + " | " + seller.getFirstName() + " " + seller.getLastName()
                    + " | " + seller.getPhone() + " | code: " + seller.getEmployeeCode()
                    + " | shift: " + seller.getShift());
        }
    }

    private void registerSale() {
        String customerId = readText("Customer id: ");
        String sellerId = readText("Seller id: ");
        String productIdsInput = readText("Product ids (comma separated): ");
        List<String> productIds = parseIds(productIdsInput);
        String accessoryIdsInput = readText("Accessory ids (comma separated, optional): ");
        productIds.addAll(parseIds(accessoryIdsInput));
        try {
            Sale sale = saleService.registerSale(customerId, sellerId, productIds);
            System.out.println("Sale " + sale.getId() + " registered. Total: $" + sale.calculateTotal());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewAllSales() {
        List<Sale> sales = saleService.viewAllSales();
        if (sales.isEmpty()) {
            System.out.println("No sales registered.");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void viewSalesByCustomer() {
        String customerId = readText("Customer id: ");
        List<Sale> sales = saleService.viewSalesByCustomer(customerId);
        if (sales.isEmpty()) {
            System.out.println("No sales found for customer " + customerId + ".");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void viewSalesBySeller() {
        String sellerId = readText("Seller id: ");
        List<Sale> sales = saleService.viewSalesBySeller(sellerId);
        if (sales.isEmpty()) {
            System.out.println("No sales found for seller " + sellerId + ".");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void printSale(Sale sale) {
        System.out.println("Sale " + sale.getId()
                + " | date: " + sale.getDate()
                + " | customer: " + sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName()
                + " | seller: " + sale.getSeller().getFirstName() + " " + sale.getSeller().getLastName()
                + " | items: " + sale.getProducts().size()
                + " | total: $" + sale.calculateTotal()
                + (sale.canBeReturned() ? " [returnable]" : " [return expired]"));
    }

    // -------------------------------------------------------------------------
    // Returns submenu
    // -------------------------------------------------------------------------

    private void returnMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== RETURNS ===");
            System.out.println("1. Register return");
            System.out.println("2. View all returns");
            System.out.println("3. View returns by customer");
            System.out.println("4. View returns by sale");
            System.out.println("0. Back");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerReturn();
                    break;
                case 2:
                    viewAllReturns();
                    break;
                case 3:
                    viewReturnsByCustomer();
                    break;
                case 4:
                    viewReturnsBySale();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerReturn() {
        String saleId = readText("Sale id to return: ");
        String productIdsInput = readText("Product ids to return (comma separated): ");
        List<String> productIds = new ArrayList<>();
        for (String item : productIdsInput.split(",")) {
            if (!item.trim().isEmpty()) {
                productIds.add(item.trim());
            }
        }
        try {
            Return ret = returnService.registerReturn(saleId, productIds);
            System.out.println("Return " + ret.getId() + " registered. Refund: $" + ret.calculateRefundAmount());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewAllReturns() {
        List<Return> returns = returnService.viewAllReturns();
        if (returns.isEmpty()) {
            System.out.println("No returns registered.");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void viewReturnsByCustomer() {
        String customerId = readText("Customer id: ");
        List<Return> returns = returnService.viewReturnsByCustomer(customerId);
        if (returns.isEmpty()) {
            System.out.println("No returns found for customer " + customerId + ".");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void viewReturnsBySale() {
        String saleId = readText("Sale id: ");
        List<Return> returns = returnService.viewReturnsBySale(saleId);
        if (returns.isEmpty()) {
            System.out.println("No returns found for sale " + saleId + ".");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void printReturn(Return ret) {
        System.out.println("Return " + ret.getId()
                + " | date: " + ret.getDate()
                + " | customer: " + ret.getCustomer().getFirstName() + " " + ret.getCustomer().getLastName()
                + " | items: " + ret.getProducts().size()
                + " | refund: $" + ret.calculateRefundAmount());
    }

    private void reportsMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== REPORTS ===");
            System.out.println("1. Monthly balance (sales - returns)");
            System.out.println("0. Back");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    monthlyBalance();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void monthlyBalance() {
        int month = readInt("Month (1-12): ");
        int year  = readInt("Year (e.g. 2026): ");
        try {
            double balance = returnService.generateMonthlyBalance(month, year);
            System.out.printf("Monthly balance for %02d/%d: $%.2f%n", month, year, balance);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void accessoryMenu() {
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
            int option = readInt("Choose an option: ");
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
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        String connectionType = readText("Connection type (WIRELESS/WIRED): ");
        String consoleIdsInput = readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = parseIds(consoleIdsInput);
        try {
            Accessory controller = accessoryService.registerController(title, price, stock, connectionType, consoleIds);
            System.out.println("Controller " + controller.getId() + " registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void registerCable() {
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        double lengthInMeters = readDouble("Length in meters: ");
        String connectorType = readText("Connector type: ");
        String consoleIdsInput = readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = parseIds(consoleIdsInput);
        try {
            Accessory cable = accessoryService.registerCable(title, price, stock, lengthInMeters, connectorType, consoleIds);
            System.out.println("Cable " + cable.getId() + " registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void registerMemory() {
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        int capacityInGb = readInt("Capacity in GB: ");
        String memoryType = readText("Memory type: ");
        String consoleIdsInput = readText("Compatible console ids (comma separated): ");
        List<String> consoleIds = parseIds(consoleIdsInput);
        try {
            Accessory memory = accessoryService.registerMemory(title, price, stock, capacityInGb, memoryType, consoleIds);
            System.out.println("Memory " + memory.getId() + " registered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
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
        String type = readText("Type (CONTROLLER/CABLE/MEMORY): ");
        try {
            List<Accessory> accessories = accessoryService.listAccessoriesByType(type);
            if (accessories.isEmpty()) {
                System.out.println("No accessories of type " + type + " registered.");
                return;
            }
            for (Accessory accessory : accessories) {
                System.out.println(accessory.getDescription());
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listAccessoriesCompatibleWith() {
        String consoleId = readText("Console id: ");
        try {
            List<Accessory> accessories = accessoryService.findAccessoriesCompatibleWith(consoleId);
            if (accessories.isEmpty()) {
                System.out.println("No accessories compatible with console " + consoleId + ".");
                return;
            }
            for (Accessory accessory : accessories) {
                System.out.println(accessory.getDescription());
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private List<String> parseIds(String input) {
        List<String> ids = new ArrayList<>();
        for (String item : input.split(",")) {
            if (!item.trim().isEmpty()) {
                ids.add(item.trim());
            }
        }
        return ids;
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            } catch (NoSuchElementException e) {
                return 0;
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            } catch (NoSuchElementException e) {
                return 0.0;
            }
        }
    }
}