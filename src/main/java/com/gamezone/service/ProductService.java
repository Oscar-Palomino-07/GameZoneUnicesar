package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.validation.ProductValidator;

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
     * <p>The data is validated through {@link ProductValidator} before the
     * video game is added: no field may be blank, the price must be positive,
     * the stock must not be negative and the id must be unique. Once
     * registered, the inventory is immediately persisted.</p>
     *
     * @param id        the unique product identifier
     * @param title     the video game title
     * @param price     the unit price of the video game
     * @param stock     the quantity available in inventory
     * @param platform  the platform the game is developed for
     * @param genre     the game genre
     * @param ageRating the recommended age rating of the game
     * @throws InvalidDataException  when any field is blank, the price is not
     *         positive or the stock is negative
     * @throws BusinessRuleException when the id already belongs to another
     *         product
     */
    public void registerVideoGame(String id, String title, double price, int stock,
                                  String platform, String genre, String ageRating) {
        ProductValidator.validateProductData(id, title, price, stock);
        ProductValidator.validateRequiredField("platform", platform);
        ProductValidator.validateRequiredField("genre", genre);
        ProductValidator.validateRequiredField("ageRating", ageRating);
        ProductValidator.validateUniqueProduct(listAllProducts(), id);
        videoGames.add(new VideoGame(id, title, price, stock, platform, genre, ageRating));
        saveAll();
    }

    /**
     * Registers a new console in the system.
     *
     * <p>The data is validated through {@link ProductValidator} before the
     * console is added: no field may be blank, the price must be positive,
     * the stock must not be negative and the id must be unique. Once
     * registered, the inventory is immediately persisted.</p>
     *
     * @param id         the unique product identifier
     * @param title      the console title or name
     * @param price      the unit price of the console
     * @param stock      the quantity available in inventory
     * @param brand      the console manufacturer brand
     * @param model      the console model
     * @param generation the console generation
     * @throws InvalidDataException  when any field is blank, the price is not
     *         positive or the stock is negative
     * @throws BusinessRuleException when the id already belongs to another
     *         product
     */
    public void registerConsole(String id, String title, double price, int stock,
                                String brand, String model, String generation) {
        ProductValidator.validateProductData(id, title, price, stock);
        ProductValidator.validateRequiredField("brand", brand);
        ProductValidator.validateRequiredField("model", model);
        ProductValidator.validateRequiredField("generation", generation);
        ProductValidator.validateUniqueProduct(listAllProducts(), id);
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
     * @throws ResourceNotFoundException when no product matches the given id
     * @throws InvalidDataException      when the quantity is negative
     */
    public void updateStock(String productId, int quantity) {
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        ProductValidator.validateNonNegativeStock(quantity);
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
     * @throws InvalidDataException      when the quantity is negative
     * @throws ResourceNotFoundException when no product matches the given id
     */
    public void restoreStock(String productId, int quantity) {
        ProductValidator.validateNonNegativeStock(quantity);
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        product.updateStock(product.getStock() + quantity);
        saveAll();
    }

    /**
     * Persists the current inventory of video games and consoles.
     */
    private void saveAll() {
        repository.saveAllVideoGames(videoGames);
        repository.saveAllConsoles(consoles);
    }
}