package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the return module of GameZone Unicesar.
 * Returns are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 *
 * <p>The repository registers custom serializers because the default Gson
 * behavior is not enough for this module: {@link LocalDate} values are
 * written as ISO-8601 strings and every returned {@link Product} is rebuilt
 * as its concrete subtype ({@link VideoGame}, {@link Console},
 * {@link Controller}, {@link Cable} or {@link Memory}) when the file is
 * loaded, so returned accessories keep their own type.</p>
 */
public class ReturnRepository {

    private static final String RETURN_FILE = "data/return.json";

    private static final Type RETURN_TYPE = new TypeToken<List<Return>>() {
    }.getType();

    private final JsonFileStore store;

    /**
     * Creates the return repository and configures the Gson instance with the
     * serializers required for {@link LocalDate} and {@link Product}.
     */
    public ReturnRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Product.class, new ProductDeserializer());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        this.store = new JsonFileStore(builder.create());
    }

    /**
     * Saves all returns to the returns JSON file, overwriting its previous
     * contents.
     *
     * @param returns the list of returns to persist
     */
    public void saveAll(List<Return> returns) {
        writeList(RETURN_FILE, returns, RETURN_TYPE);
    }

    /**
     * Loads all returns from the returns JSON file.
     *
     * @return the list of stored returns, or an empty list if the file does
     *         not exist or cannot be parsed
     */
    public List<Return> loadAll() {
        return readList(RETURN_FILE, RETURN_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of returns to persist
     * @param type     the Gson type of the generic list
     */
    private void writeList(String fileName, List<?> items, Type type) {
        try {
            store.write(fileName, items, type);
        } catch (IOException e) {
            System.err.println("Error saving returns to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of returns from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the concrete type stored in the file
     * @return the stored returns, or an empty list if the file does not exist
     *         or cannot be parsed
     */
    private <T> List<T> readList(String fileName, Type type) {
        try {
            return store.read(fileName, type);
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading returns from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
