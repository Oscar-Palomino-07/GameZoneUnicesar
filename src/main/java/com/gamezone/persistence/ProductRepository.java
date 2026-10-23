package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the product module of GameZone Unicesar.
 * Video games and consoles are stored separately in two JSON files under the
 * {@code data} directory, serialized with Gson. If a file does not exist yet,
 * the repository loads an empty list.
 */
public class ProductRepository {

    private static final String VIDEO_GAMES_FILE = "data/videogames.json";
    private static final String CONSOLES_FILE = "data/consoles.json";

    private static final Type VIDEO_GAMES_TYPE = new TypeToken<List<VideoGame>>() {
    }.getType();
    private static final Type CONSOLES_TYPE = new TypeToken<List<Console>>() {
    }.getType();

    private final JsonFileStore store = new JsonFileStore(new GsonBuilder().setPrettyPrinting().create());

    /**
     * Saves all products to their respective JSON files (video games and
     * consoles), separating them by subtype and keeping the passed list
     * aligned with the persisted order (video games first, then consoles).
     *
     * @param products the list of products to persist
     */
    public void saveAllProducts(List<Product> products) {
        List<VideoGame> videoGames = new ArrayList<>();
        List<Console> consoles = new ArrayList<>();
        for (Product product : products) {
            if (product instanceof VideoGame) {
                videoGames.add((VideoGame) product);
            } else if (product instanceof Console) {
                consoles.add((Console) product);
            }
        }
        saveAllVideoGames(videoGames);
        saveAllConsoles(consoles);
        products.clear();
        products.addAll(videoGames);
        products.addAll(consoles);
    }

    /**
     * Loads all stored products from disk, returning video games followed by
     * consoles.
     *
     * @return the unified list of stored products
     */
    public List<Product> loadAllProducts() {
        List<Product> products = new ArrayList<>();
        products.addAll(loadAllVideoGames());
        products.addAll(loadAllConsoles());
        return products;
    }

    /**
     * Saves all video games to the video games JSON file, overwriting its
     * previous contents.
     *
     * @param videoGames the list of video games to persist
     */
    public void saveAllVideoGames(List<VideoGame> videoGames) {
        writeList(VIDEO_GAMES_FILE, videoGames, VIDEO_GAMES_TYPE);
    }

    /**
     * Loads all video games from the video games JSON file.
     *
     * @return the list of stored video games, or an empty list if the file
     *         does not exist
     */
    public List<VideoGame> loadAllVideoGames() {
        return readList(VIDEO_GAMES_FILE, VIDEO_GAMES_TYPE);
    }

    /**
     * Saves all consoles to the consoles JSON file, overwriting its previous
     * contents.
     *
     * @param consoles the list of consoles to persist
     */
    public void saveAllConsoles(List<Console> consoles) {
        writeList(CONSOLES_FILE, consoles, CONSOLES_TYPE);
    }

    /**
     * Loads all consoles from the consoles JSON file.
     *
     * @return the list of stored consoles, or an empty list if the file does
     *         not exist
     */
    public List<Console> loadAllConsoles() {
        return readList(CONSOLES_FILE, CONSOLES_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of products to persist
     * @param type     the Gson type of the generic list
     * @throws PersistenceException when the file cannot be written
     */
    private void writeList(String fileName, List<?> items, Type type) {
        try {
            store.write(fileName, items, type);
        } catch (IOException e) {
            throw new PersistenceException(fileName, "escritura", e);
        }
    }

    /**
     * Deserializes a list of products from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the concrete product type stored in the file
     * @return the stored products, or an empty list if the file does not
     *         exist yet
     * @throws PersistenceException when the file exists but cannot be read
     *         or parsed
     */
    private <T> List<T> readList(String fileName, Type type) {
        try {
            return store.read(fileName, type);
        } catch (IOException | JsonSyntaxException e) {
            throw new PersistenceException(fileName, "lectura", e);
        }
    }
}
