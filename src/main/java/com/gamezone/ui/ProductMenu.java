package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.service.ProductFactory;
import com.gamezone.service.ProductService;

import java.util.List;

/**
 * Submenu of the product module: registers video games and consoles and lists
 * the inventory. Every option delegates to the {@link ProductService}.
 */
public class ProductMenu implements Menu {

    private final ProductService productService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the product menu.
     *
     * @param productService the service used for product operations
     * @param input          the reader of the user input
     * @param errorReporter  the reporter that turns errors into messages
     */
    public ProductMenu(ProductService productService, ConsoleInput input, ConsoleErrorReporter errorReporter) {
        this.productService = productService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Products";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== PRODUCTS ===");
            System.out.println("1. Register video game");
            System.out.println("2. Register console");
            System.out.println("3. List all products");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
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

    private void registerVideoGame() {
        String id = input.readText("Product id: ");
        String title = input.readText("Title: ");
        double price = input.readDouble("Price: ");
        int stock = input.readInt("Stock: ");
        String platform = input.readText("Platform: ");
        String genre = input.readText("Genre: ");
        String ageRating = input.readText("Age rating: ");
        errorReporter.runGuarded(() -> {
            productService.registerProduct(
                    ProductFactory.createVideoGame(id, title, price, stock, platform, genre, ageRating));
            System.out.println(productService.findById(id) != null
                    ? "Video game registered."
                    : "Video game could not be registered.");
        });
    }

    private void registerConsole() {
        String id = input.readText("Product id: ");
        String title = input.readText("Title: ");
        double price = input.readDouble("Price: ");
        int stock = input.readInt("Stock: ");
        String brand = input.readText("Brand: ");
        String model = input.readText("Model: ");
        String generation = input.readText("Generation: ");
        errorReporter.runGuarded(() -> {
            productService.registerProduct(
                    ProductFactory.createConsole(id, title, price, stock, brand, model, generation));
            System.out.println(productService.findById(id) != null
                    ? "Console registered."
                    : "Console could not be registered.");
        });
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
}
