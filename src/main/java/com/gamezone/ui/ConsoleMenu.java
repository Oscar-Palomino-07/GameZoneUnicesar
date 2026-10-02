package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private final WarrantyService warrantyService;
    private final PromotionService promotionService;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates the console menu with all seven services of the application.
     *
     * @param productService  the service used for product operations
     * @param personService   the service used for person operations
     * @param saleService     the service used for sale operations
     * @param returnService   the service used for return operations
     * @param accessoryService the service used for accessory operations
     * @param warrantyService the service used for warranty queries
     * @param promotionService the service used for promotion operations
     */
    public ConsoleMenu(ProductService productService, PersonService personService,
                       SaleService saleService, ReturnService returnService, AccessoryService accessoryService,
                       WarrantyService warrantyService, PromotionService promotionService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.returnService = returnService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
        this.promotionService = promotionService;
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
            System.out.println("7. Gestión de garantías");
            System.out.println("8. Gestión de promociones");
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
                case 7:
                    warrantyMenu();
                    break;
                case 8:
                    promotionMenu();
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
            System.out.println("5. Ver detalle de una venta (recibo)");
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
                case 5:
                    viewSaleDetail();
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
        List<String> extendedWarrantyIds = askExtendedWarranties(productIds);
        try {
            Sale sale = saleService.registerSale(customerId, sellerId, productIds, extendedWarrantyIds);
            System.out.println("Venta " + sale.getId() + " registrada.");
            System.out.println(sale.generateReceipt());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Asks the user, for every distinct console included in the sale, whether
     * the extended warranty must be added.
     *
     * @param itemIds the identifiers of the items entered for the sale
     * @return the identifiers of the consoles for which the user accepted the
     *         extended warranty
     */
    private List<String> askExtendedWarranties(List<String> itemIds) {
        List<String> extendedWarrantyIds = new ArrayList<>();
        for (String itemId : new LinkedHashSet<>(itemIds)) {
            Product item = productService.findById(itemId);
            if (item instanceof Console) {
                String answer = readText("¿Desea garantía extendida para la consola " + item.getTitle()
                        + " (" + itemId + ")? (s/n): ");
                if (answer.equalsIgnoreCase("s")) {
                    extendedWarrantyIds.add(itemId);
                }
            }
        }
        return extendedWarrantyIds;
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

    private void viewSaleDetail() {
        String saleId = readText("Id de la venta: ");
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            System.out.println("No se encontró la venta " + saleId + ".");
            return;
        }
        System.out.println(sale.generateReceipt());
    }

    private void printSale(Sale sale) {
        System.out.println("Sale " + sale.getId()
                + " | date: " + sale.getDate()
                + " | customer: " + sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName()
                + " | seller: " + sale.getSeller().getFirstName() + " " + sale.getSeller().getLastName()
                + " | items: " + sale.getProducts().size()
                + " | total: $" + sale.calculateTotal()
                + (sale.getAppliedPromotionName() != null && sale.getDiscountAmount() > 0
                        ? " | promoción: " + sale.getAppliedPromotionName() + " (-$" + sale.getDiscountAmount() + ")"
                        : "")
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
            System.out.println("1. Balance mensual (ventas - devoluciones)");
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
        int month = readInt("Mes (1-12): ");
        int year  = readInt("Año (ej. 2026): ");
        try {
            double totalSales = returnService.calculateMonthlySales(month, year);
            double totalReturns = returnService.calculateMonthlyReturns(month, year);
            double balance = returnService.generateMonthlyBalance(month, year);
            System.out.printf("Balance mensual %02d/%d%n", month, year);
            System.out.printf("  Total de ventas:       $%.2f%n", totalSales);
            System.out.printf("  Total de devoluciones: $%.2f%n", totalReturns);
            System.out.printf("  Balance neto:          $%.2f%n", balance);
        } catch (IllegalArgumentException e) {
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

    // -------------------------------------------------------------------------
    // Warranties submenu
    // -------------------------------------------------------------------------

    private void warrantyMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== GESTIÓN DE GARANTÍAS ===");
            System.out.println("1. Consultar la garantía de un producto en una venta");
            System.out.println("2. Listar todas las garantías");
            System.out.println("3. Listar garantías vigentes");
            System.out.println("4. Listar garantías próximas a vencer");
            System.out.println("0. Volver");
            int option = readInt("Seleccione una opción: ");
            switch (option) {
                case 1:
                    viewWarrantyByProduct();
                    break;
                case 2:
                    printWarranties(warrantyService.listAllWarranties(), "No hay garantías registradas.");
                    break;
                case 3:
                    printWarranties(warrantyService.listActiveWarranties(), "No hay garantías vigentes.");
                    break;
                case 4:
                    viewWarrantiesExpiringSoon();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    private void viewWarrantyByProduct() {
        String saleId = readText("Id de la venta: ");
        String productId = readText("Id del producto: ");
        Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
        if (warranty == null) {
            System.out.println("No se encontró una garantía para el producto " + productId
                    + " en la venta " + saleId + ".");
            return;
        }
        System.out.println(warranty.generateWarrantyCertificate());
    }

    private void viewWarrantiesExpiringSoon() {
        int daysAhead = readInt("¿Cuántos días de anticipación? ");
        try {
            printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead),
                    "No hay garantías que venzan en los próximos " + daysAhead + " días.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printWarranties(List<Warranty> warranties, String emptyMessage) {
        if (warranties.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Warranty warranty : warranties) {
            System.out.println(warranty.getId()
                    + " | " + warranty.getWarrantyType()
                    + " | Producto: " + warranty.getProduct().getTitle() + " (" + warranty.getProduct().getId() + ")"
                    + " | Venta: " + warranty.getSale().getId()
                    + " | Inicio: " + warranty.getStartDate()
                    + " | Vence: " + warranty.getEndDate()
                    + " | Estado: " + (warranty.isActive(LocalDate.now()) ? "Vigente" : "Expirada"));
        }
    }

    // -------------------------------------------------------------------------
    // Promotions submenu
    // -------------------------------------------------------------------------

    private void promotionMenu() {
        boolean running = true;
        while (running) {
            System.out.println("=== GESTIÓN DE PROMOCIONES ===");
            System.out.println("1. Registrar promoción por porcentaje");
            System.out.println("2. Registrar promoción por categoría");
            System.out.println("3. Registrar promoción por volumen de compra");
            System.out.println("4. Listar todas las promociones");
            System.out.println("5. Listar promociones vigentes");
            System.out.println("0. Volver");
            int option = readInt("Seleccione una opción: ");
            switch (option) {
                case 1:
                    registerPercentagePromotion();
                    break;
                case 2:
                    registerCategoryPromotion();
                    break;
                case 3:
                    registerBulkPurchasePromotion();
                    break;
                case 4:
                    printPromotions(promotionService.listAllPromotions(), "No hay promociones registradas.");
                    break;
                case 5:
                    printPromotions(promotionService.listActivePromotions(LocalDate.now()),
                            "No hay promociones vigentes hoy.");
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    private void registerPercentagePromotion() {
        String name = readText("Nombre de la promoción: ");
        double percentage = readDouble("Porcentaje de descuento (0-100): ");
        LocalDate startDate = readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        try {
            Promotion promotion = promotionService.registerPercentage(name, percentage, startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void registerCategoryPromotion() {
        String name = readText("Nombre de la promoción: ");
        double percentage = readDouble("Porcentaje de descuento (0-100): ");
        String category = readPromotionCategory();
        if (category == null) {
            return;
        }
        LocalDate startDate = readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        try {
            Promotion promotion = promotionService.registerCategory(name, percentage, category, startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // Lets the user pick the target category of a category promotion.
    private String readPromotionCategory() {
        System.out.println("Categoría:");
        System.out.println("1. Videojuegos");
        System.out.println("2. Consolas");
        System.out.println("3. Accesorios");
        switch (readInt("Seleccione una opción: ")) {
            case 1:
                return "VIDEOGAME";
            case 2:
                return "CONSOLE";
            case 3:
                return "ACCESSORY";
            default:
                System.out.println("Categoría no válida.");
                return null;
        }
    }

    private void registerBulkPurchasePromotion() {
        String name = readText("Nombre de la promoción: ");
        double percentage = readDouble("Porcentaje de descuento (0-100): ");
        int minimumQuantity = readInt("Cantidad mínima de productos: ");
        LocalDate startDate = readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        try {
            Promotion promotion = promotionService.registerBulkPurchase(name, percentage, minimumQuantity,
                    startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Promotion promotion : promotions) {
            System.out.println(promotion.getId()
                    + " | " + promotion.getName()
                    + " | " + promotion.getDiscountPercentage() + "%"
                    + " | Vigencia: " + promotion.getStartDate() + " a " + promotion.getEndDate()
                    + " | Estado: " + (promotion.isActive() ? "Vigente" : "No vigente"));
        }
    }

    /**
     * Reads a date typed by the user in ISO format.
     *
     * @param prompt the text shown to the user
     * @return the parsed date, or {@code null} when the text is not a valid
     *         date (after telling the user so)
     */
    private LocalDate readDate(String prompt) {
        String text = readText(prompt);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida: use el formato AAAA-MM-DD.");
            return null;
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