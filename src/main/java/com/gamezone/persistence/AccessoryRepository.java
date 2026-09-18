package com.gamezone.persistence;

import com.gamezone.model.Accessory;
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

import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handles file-based persistence for the accessory module of GameZone Unicesar.
 * Accessories are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 */
public class AccessoryRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String ACCESSORIES_FILE = "data/accessories.json";


    private static final Type ACCESSORIES_TYPE = new TypeToken<List<Accessory>>() {
    }.getType();


    private final Gson gson;

    /**
     * Creates the accessory repository and configures the Gson instance with
     * the deserializer required for {@link Accessory}.
     */
    public AccessoryRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();

        builder.registerTypeAdapter(Accessory.class, new AccessoryDeserializer());
        this.gson = builder.create();
    }

    /**
     * Saves all accessories to the accessories JSON file, overwriting its
     * previous contents.
     *
     * @param accessories the list of accessories to persist
     */
    public void saveAll(List<Accessory> accessories) {
        writeList(ACCESSORIES_FILE, accessories, ACCESSORIES_TYPE);
    }

    /**
     * Loads all accessories from the accessories JSON file.
     *
     * @return the list of stored accessories, or an empty list if the file
     *         does not exist or cannot be parsed
     */
    public List<Accessory> loadAll() {
        return readList(ACCESSORIES_FILE, ACCESSORIES_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of accessories to persist
     * @param type     the Gson type of the generic list
     */
    private void writeList(String fileName, List<?> items, Type type) {
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(items, type, writer); // lista Java → texto JSON
            }
        } catch (IOException e) {
            System.err.println("Error saving accessories to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of accessories from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the type stored in the file
     * @return the stored accessories, or an empty list if the file does not
     *         exist or cannot be parsed
     */
    private <T> List<T> readList(String fileName, Type type) {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, type); // texto JSON → lista Java
            return items != null ? items : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading accessories from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Rebuilds an {@link Accessory} from its JSON representation, choosing the
     * concrete subtype based on the attributes present in the serialized object.
     */
    private static class AccessoryDeserializer implements JsonDeserializer<Accessory> {


        private final Gson typeGson = new Gson();

        @Override
        public Accessory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();

            if (object.has("connectionType")) {
                return typeGson.fromJson(json, Controller.class);
            }
            if (object.has("lengthInMeters") || object.has("connectorType")) {
                return typeGson.fromJson(json, Cable.class);
            }
            return typeGson.fromJson(json, Memory.class);
        }
    }

}