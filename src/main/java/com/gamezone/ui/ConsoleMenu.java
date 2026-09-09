package com.gamezone.ui;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
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
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates the console menu with the three services of the application.
     *
     * @param productService the service used for product operations
     * @param personService  the service used for person operations
     * @param saleService    the service used for sale operations
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
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
        productService.registerVideoGame(id, title, price, stock, platform, genre, ageRating);
        System.out.println(productService.findById(id) != null
                ? "Video game registered."
                : "Video game could not be registered.");
    }

    private void registerConsole() {
        String id = readText("Product id: ");
        String title = readText("Title: ");
        double price = readDouble("Price: ");
        int stock = readInt("Stock: ");
        String brand = readText("Brand: ");
        String model = readText("Model: ");
        String generation = readText("Generation: ");
        productService.registerConsole(id, title, price, stock, brand, model, generation);
        System.out.println(productService.findById(id) != null
                ? "Console registered."
                : "Console could not be registered.");
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
        List<String> productIds = new ArrayList<>();
        for (String item : productIdsInput.split(",")) {
            if (!item.trim().isEmpty()) {
                productIds.add(item.trim());
            }
        }
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
                + " | total: $" + sale.calculateTotal());
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