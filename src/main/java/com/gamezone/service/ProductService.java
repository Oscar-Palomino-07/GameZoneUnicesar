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
     * Registers a new video game in the system.
     *
     * <p>The data is validated before the video game is added: no field may
     * be blank, the id must be unique and the price and stock must not be
     * negative. Once registered, the inventory is immediately persisted.</p>
     *
     * @param id        the unique product identifier
     * @param title     the video game title
     * @param price     the unit price of the video game
     * @param stock     the quantity available in inventory
     * @param platform  the platform the game is developed for
     * @param genre     the game genre
     * @param ageRating the recommended age rating of the game
     * @throws IllegalArgumentException when any field is blank, the id already
     *         belongs to another product, or the price or stock are negative
     */
    public void registerVideoGame(String id, String title, double price, int stock,
                                  String platform, String genre, String ageRating) {
        if (isBlank(id) || isBlank(title) || isBlank(platform) || isBlank(genre) || isBlank(ageRating)) {
            throw new IllegalArgumentException("All video game fields are required.");
        }
        validate(id, price, stock);
        videoGames.add(new VideoGame(id, title, price, stock, platform, genre, ageRating));
        saveAll();
    }

    /**
     * Registers a new console in the system.
     *
     * <p>The data is validated before the console is added: no field may be
     * blank, the id must be unique and the price and stock must not be
     * negative. Once registered, the inventory is immediately persisted.</p>
     *
     * @param id         the unique product identifier
     * @param title      the console title or name
     * @param price      the unit price of the console
     * @param stock      the quantity available in inventory
     * @param brand      the console manufacturer brand
     * @param model      the console model
     * @param generation the console generation
     * @throws IllegalArgumentException when any field is blank, the id already
     *         belongs to another product, or the price or stock are negative
     */
    public void registerConsole(String id, String title, double price, int stock,
                                String brand, String model, String generation) {
        if (isBlank(id) || isBlank(title) || isBlank(brand) || isBlank(model) || isBlank(generation)) {
            throw new IllegalArgumentException("All console fields are required.");
        }
        validate(id, price, stock);
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
     * quantity and persists the change.
     *
     * @param productId the identifier of the product to update
     * @param quantity  the new quantity available in inventory
     * @throws IllegalArgumentException when no product matches the given id or
     *         the quantity is negative
     */
    public void updateStock(String productId, int quantity) {
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        product.updateStock(quantity);
        saveAll();
    }

    /**
     * Increments the stock of the product with the given identifier by the
     * specified quantity and persists the change immediately.
     *
     * <p>This method is intended to be called when a return is processed, so
     * that the returned units are put back into the available inventory.</p>
     *
     * @param productId the identifier of the product whose stock is restored
     * @param quantity  the number of units to add back to the stock
     * @throws IllegalArgumentException when no product matches the given id or
     *         the quantity is negative
     */
    public void restoreStock(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to restore cannot be negative.");
        }
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        product.updateStock(product.getStock() + quantity);
        saveAll();
    }

    /**
     * Validates that the identifier is not already registered and that the
     * price and stock are not negative.
     *
     * @param id    the identifier being registered
     * @param price the price being registered
     * @param stock the stock being registered
     * @throws IllegalArgumentException when the id already belongs to another
     *         product, or the price or stock are negative
     */
    private void validate(String id, double price, int stock) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("A product with id " + id + " already exists.");
        }
        if (price < 0 || stock < 0) {
            throw new IllegalArgumentException("Price and stock cannot be negative.");
        }
    }

    /**
     * Tells whether a field should be considered blank or invalid.
     *
     * @param value the value to check
     * @return {@code true} when the value is null or only contains spaces
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Persists the current inventory of video games and consoles.
     */
    private void saveAll() {
        repository.saveAllVideoGames(videoGames);
        repository.saveAllConsoles(consoles);
    }
}