package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.VideoGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the product module of GameZone Unicesar.
 * Video games and consoles are stored separately in two JSON files under the
 * {@code data} directory, serialized with Gson. If a file does not exist yet,
 * the repository loads an empty list.
 */
public class ProductRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String VIDEO_GAMES_FILE = "data/videogames.json";
    private static final String CONSOLES_FILE = "data/consoles.json";

    private static final Type VIDEO_GAMES_TYPE = new TypeToken<List<VideoGame>>() {
    }.getType();
    private static final Type CONSOLES_TYPE = new TypeToken<List<Console>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
     */
    private void writeList(String fileName, List<?> items, Type type) {
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(items, type, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving products to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of products from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the concrete product type stored in the file
     * @return the stored products, or an empty list if the file does not
     *         exist or cannot be parsed
     */
    private <T> List<T> readList(String fileName, Type type) {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, type);
            return items != null ? items : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading products from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}