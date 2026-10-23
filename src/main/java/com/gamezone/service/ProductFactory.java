package com.gamezone.service;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Console;
import com.gamezone.model.VideoGame;
import com.gamezone.validation.ProductValidator;

/**
 * Pure Fabrication responsible for validating subtype-specific attributes and
 * instantiating concrete {@link com.gamezone.model.Product} subclasses.
 *
 * <p>Keeps the user interface decoupled from concrete domain constructors while
 * preserving the validation order (common product fields first, followed by
 * subtype-specific fields).</p>
 */
public class ProductFactory {

    /**
     * Creates a new product factory.
     */
    public ProductFactory() {
    }

    /**
     * Validates the common and video-game-specific attributes and creates a
     * {@link VideoGame} instance.
     *
     * @param id        the unique product identifier
     * @param title     the video game title
     * @param price     the unit price of the video game
     * @param stock     the quantity available in inventory
     * @param platform  the platform the game is developed for
     * @param genre     the game genre
     * @param ageRating the recommended age rating of the game
     * @return the validated {@link VideoGame} instance
     * @throws InvalidDataException when any field is blank, the price is not
     *         positive or the stock is negative
     */
    public static VideoGame createVideoGame(String id, String title, double price, int stock,
                                            String platform, String genre, String ageRating) {
        ProductValidator.validateProductData(id, title, price, stock);
        ProductValidator.validateRequiredField("platform", platform);
        ProductValidator.validateRequiredField("genre", genre);
        ProductValidator.validateRequiredField("ageRating", ageRating);
        return new VideoGame(id, title, price, stock, platform, genre, ageRating);
    }

    /**
     * Validates the common and console-specific attributes and creates a
     * {@link Console} instance.
     *
     * @param id         the unique product identifier
     * @param title      the console title or name
     * @param price      the unit price of the console
     * @param stock      the quantity available in inventory
     * @param brand      the console manufacturer brand
     * @param model      the console model
     * @param generation the console generation
     * @return the validated {@link Console} instance
     * @throws InvalidDataException when any field is blank, the price is not
     *         positive or the stock is negative
     */
    public static Console createConsole(String id, String title, double price, int stock,
                                        String brand, String model, String generation) {
        ProductValidator.validateProductData(id, title, price, stock);
        ProductValidator.validateRequiredField("brand", brand);
        ProductValidator.validateRequiredField("model", model);
        ProductValidator.validateRequiredField("generation", generation);
        return new Console(id, title, price, stock, brand, model, generation);
    }
}
