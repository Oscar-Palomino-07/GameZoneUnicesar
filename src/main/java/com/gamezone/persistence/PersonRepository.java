package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Handles the JSON-based persistence of the people managed by the store.
 *
 * <p>Customers and sellers are stored in two JSON files inside the
 * {@code data} directory, one file per type: {@code customers.json} and
 * {@code sellers.json}. The serialization is delegated to the Gson
 * library, configured to produce indented JSON for readability.</p>
 *
 * <p>This class belongs to the persistence layer and does not contain any
 * business rule; its only responsibility is to save and recover people
 * from files.</p>
 */
public class PersonRepository {

    private static final Path DATA_DIRECTORY = Paths.get("data");
    private static final Path CUSTOMERS_FILE = DATA_DIRECTORY.resolve("customers.json");
    private static final Path SELLERS_FILE = DATA_DIRECTORY.resolve("sellers.json");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the given customers to the customers file, overwriting its
     * previous content.
     *
     * @param customers the customers to persist
     */
    public void saveAllCustomers(List<Customer> customers) {
        writeFile(gson.toJson(customers), CUSTOMERS_FILE);
    }

    /**
     * Saves the given sellers to the sellers file, overwriting its
     * previous content.
     *
     * @param sellers the sellers to persist
     */
    public void saveAllSellers(List<Seller> sellers) {
        writeFile(gson.toJson(sellers), SELLERS_FILE);
    }

    /**
     * Loads all customers stored in the customers file.
     *
     * <p>If the file does not exist yet, an empty list is returned so that
     * the system can start with an empty customer registry.</p>
     *
     * @return the list of stored customers
     */
    public List<Customer> loadAllCustomers() {
        Type type = new TypeToken<List<Customer>>() { }.getType();
        return readGenericList(CUSTOMERS_FILE, type);
    }

    /**
     * Loads all sellers stored in the sellers file.
     *
     * <p>If the file does not exist yet, an empty list is returned.</p>
     *
     * @return the list of stored sellers
     */
    public List<Seller> loadAllSellers() {
        Type type = new TypeToken<List<Seller>>() { }.getType();
        return readGenericList(SELLERS_FILE, type);
    }

    /**
     * Reads the given JSON file and deserializes it into a list of people.
     *
     * @param file the file to read
     * @param type the generic list type expected by Gson
     * @return the deserialized list, or an empty list when the file is
     *         missing or empty
     */
    private <T> List<T> readGenericList(Path file, Type type) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        if (Files.getSize(file) == 0) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<T> result = gson.fromJson(reader, type);
            return result == null ? new ArrayList<>() : result;
        } catch (IOException exception) {
            throw new RuntimeException("Error loading people from " + file, exception);
        }
    }

    /**
     * Writes the given JSON content to the specified file, creating the
     * parent directory when required.
     *
     * @param json the JSON content to persist
     * @param file the target data file
     */
    private void writeFile(String json, Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error saving people to " + file, exception);
        }
    }
}