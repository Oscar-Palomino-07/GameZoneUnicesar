package com.gamezone.persistence;

import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.VideoGame;
import com.google.gson.*;
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

    private static final String DATA_DIRECTORY = "data";
    private static final String RETURN_FILE = "data/return.json";

    private static final Type RETURN_TYPE = new TypeToken<List<Return>>() {
    }.getType();

    private final Gson gson;

    /**
     * Creates the return repository and configures the Gson instance with the
     * serializers required for {@link LocalDate} and {@link Product}.
     */
    public ReturnRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Product.class, new ProductDeserializer());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        this.gson = builder.create();
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
            Path path = Paths.get(fileName);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(items, type, writer);
            }
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
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, type);
            return items != null ? items : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading returns from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Rebuilds a {@link Product} from its JSON representation, choosing the
     * concrete subtype based on the attributes present in the serialized
     * object. Accessories are checked first because their attributes never
     * appear in video games or consoles.
     */
    private static class ProductDeserializer implements JsonDeserializer<Product> {

        private final Gson typeGson = new Gson();

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("connectionType")) {
                return typeGson.fromJson(json, Controller.class);
            }
            if (object.has("lengthInMeters") || object.has("connectorType")) {
                return typeGson.fromJson(json, Cable.class);
            }
            if (object.has("capacityInGb") || object.has("memoryType")) {
                return typeGson.fromJson(json, Memory.class);
            }
            if (object.has("platform") || object.has("genre") || object.has("ageRating")) {
                return typeGson.fromJson(json, VideoGame.class);
            }
            return typeGson.fromJson(json, Console.class);
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
