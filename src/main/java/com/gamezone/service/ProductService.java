package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the business operations for the product module of GameZone Unicesar:
 * registering video games and consoles, listing the inventory, finding a
 * product by identifier and updating its stock. Every successful change is
 * persisted immediately through the {@link ProductRepository}.
 */
public class ProductService {

    private final ProductRepository repository;
    private final List<VideoGame> videoGames = new ArrayList<>();
    private final List<Console> consoles = new ArrayList<>();

    /**
     * Creates a product service that uses the given repository and loads the
     * previously stored products.
     *
     * @param repository the repository used to persist and load products
     */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
        this.videoGames.addAll(repository.loadAllVideoGames());
        this.consoles.addAll(repository.loadAllConsoles());
    }

    /**
     * Registers a new video game, rejecting the operation when the identifier
     * is already in use or the price or stock are negative.
     *
     * @param id        the unique product identifier
     * @param title     the video game title
     * @param price     the unit price of the video game
     * @param stock     the quantity available in inventory
     * @param platform  the platform the game is developed for
     * @param genre     the game genre
     * @param ageRating the recommended age rating of the game
     */
    public void registerVideoGame(String id, String title, double price, int stock,
                                  String platform, String genre, String ageRating) {
        if (!isValid(id, price, stock)) {
            return;
        }
        videoGames.add(new VideoGame(id, title, price, stock, platform, genre, ageRating));
        saveAll();
    }

    /**
     * Registers a new console, rejecting the operation when the identifier is
     * already in use or the price or stock are negative.
     *
     * @param id         the unique product identifier
     * @param title      the console title or name
     * @param price      the unit price of the console
     * @param stock      the quantity available in inventory
     * @param brand      the console manufacturer brand
     * @param model      the console model
     * @param generation the console generation
     */
    public void registerConsole(String id, String title, double price, int stock,
                                String brand, String model, String generation) {
        if (!isValid(id, price, stock)) {
            return;
        }
        consoles.add(new Console(id, title, price, stock, brand, model, generation));
        saveAll();
    }

    /**
     * @return the list of all products currently available in inventory
     */
    public List<Product> listAllProducts() {
        List<Product> products = new ArrayList<>();
        products.addAll(videoGames);
        products.addAll(consoles);
        return products;
    }

    /**
     * Finds a product by its identifier, searching both video games and
     * consoles.
     *
     * @param id the identifier to look for
     * @return the matching product, or {@code null} if it is not registered
     */
    public Product findById(String id) {
        for (Product product : listAllProducts()) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Updates the stock of the product with the given identifier to the given
     * quantity and persists the change. If the product does not exist, the
     * operation is discarded.
     *
     * @param productId the identifier of the product to update
     * @param quantity  the new quantity available in inventory
     */
    public void updateStock(String productId, int quantity) {
        Product product = findById(productId);
        if (product == null) {
            System.err.println("Product not found: " + productId);
            return;
        }
        product.updateStock(quantity);
        saveAll();
    }

    /**
     * Validates that the identifier is not already registered and that the
     * price and stock are not negative.
     *
     * @param id    the identifier being registered
     * @param price the price being registered
     * @param stock the stock being registered
     * @return {@code true} when the product data is valid
     */
    private boolean isValid(String id, double price, int stock) {
        if (findById(id) != null) {
            System.err.println("A product with that id already exists: " + id);
            return false;
        }
        if (price < 0 || stock < 0) {
            System.err.println("Price and stock cannot be negative.");
            return false;
        }
        return true;
    }

    /**
     * Persists the current inventory of video games and consoles.
     */
    private void saveAll() {
        repository.saveAllVideoGames(videoGames);
        repository.saveAllConsoles(consoles);
    }
}