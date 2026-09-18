package com.gamezone.persistence;

import com.gamezone.model.Promotion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the promotion module of GameZone Unicesar.
 * Promotions are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 *
 * <p>The repository registers a custom adapter so that {@link LocalDate}
 * values are written as ISO-8601 strings.</p>
 */
public class PromotionRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String PROMOTIONS_FILE = "data/promotions.json";

    private static final Type PROMOTIONS_TYPE = new TypeToken<List<Promotion>>() {
    }.getType();

    private final Gson gson;

    /**
     * Creates the promotion repository and configures the Gson instance with
     * the adapter required for {@link LocalDate}.
     */
    public PromotionRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        this.gson = builder.create();
    }

    /**
     * Saves all promotions to the promotions JSON file, overwriting its
     * previous contents.
     *
     * @param promotions the list of promotions to persist
     */
    public void saveAll(List<Promotion> promotions) {
        writeList(PROMOTIONS_FILE, promotions, PROMOTIONS_TYPE);
    }

    /**
     * Loads all promotions from the promotions JSON file.
     *
     * @return the list of stored promotions, or an empty list if the file
     *         does not exist or cannot be parsed
     */
    public List<Promotion> loadAll() {
        return readList(PROMOTIONS_FILE, PROMOTIONS_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of promotions to persist
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
            System.err.println("Error saving promotions to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of promotions from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the type stored in the file
     * @return the stored promotions, or an empty list if the file does not
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
            System.err.println("Error loading promotions from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Serializes {@link LocalDate} values as ISO-8601 strings instead of the
     * default Gson object representation.
     */
    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString());
        }
    }
}