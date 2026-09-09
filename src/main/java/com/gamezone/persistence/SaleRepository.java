package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.VideoGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
 * Handles file-based persistence for the sales module of GameZone Unicesar.
 * Sales are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 *
 * <p>The repository registers custom serializers because the default Gson
 * behavior is not enough for this module: {@link LocalDate} values are
 * written as ISO-8601 strings and every {@link Product} is rebuilt as its
 * concrete subtype ({@link VideoGame} or {@link Console}) when a file is
 * loaded.</p>
 */
public class SaleRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String SALES_FILE = "data/sales.json";

    private static final Type SALES_TYPE = new TypeToken<List<Sale>>() {
    }.getType();

    private final Gson gson;

    /**
     * Creates the sale repository and configures the Gson instance with the
     * serializers required for {@link LocalDate} and {@link Product}.
     */
    public SaleRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Product.class, new ProductDeserializer());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        this.gson = builder.create();
    }

    /**
     * Saves all sales to the sales JSON file, overwriting its previous
     * contents.
     *
     * @param sales the list of sales to persist
     */
    public void saveAll(List<Sale> sales) {
        writeList(SALES_FILE, sales, SALES_TYPE);
    }

    /**
     * Loads all sales from the sales JSON file.
     *
     * @return the list of stored sales, or an empty list if the file does
     *         not exist or cannot be parsed
     */
    public List<Sale> loadAll() {
        return readList(SALES_FILE, SALES_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of sales to persist
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
            System.err.println("Error saving sales to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of sales from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the concrete type stored in the file
     * @return the stored sales, or an empty list if the file does not exist
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
            System.err.println("Error loading sales from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Rebuilds a {@link Product} from its JSON representation, choosing the
     * concrete subtype ({@link VideoGame} or {@link Console}) based on the
     * attributes present in the serialized object.
     */
    private static class ProductDeserializer implements JsonDeserializer<Product> {

        private final Gson typeGson = new Gson();

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
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