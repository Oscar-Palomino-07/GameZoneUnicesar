package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
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
 * Handles file-based persistence for the person module of GameZone Unicesar.
 * Customers and sellers are stored separately in two JSON files under the
 * {@code data} directory, serialized with Gson. If a file does not exist yet,
 * the repository loads an empty list.
 */
public class PersonRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String CUSTOMERS_FILE = "data/customers.json";
    private static final String SELLERS_FILE = "data/sellers.json";

    private static final Type CUSTOMERS_TYPE = new TypeToken<List<Customer>>() {
    }.getType();
    private static final Type SELLERS_TYPE = new TypeToken<List<Seller>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves all customers to the customers JSON file, overwriting its
     * previous contents.
     *
     * @param customers the list of customers to persist
     */
    public void saveAllCustomers(List<Customer> customers) {
        writeList(CUSTOMERS_FILE, customers, CUSTOMERS_TYPE);
    }

    /**
     * Loads all customers from the customers JSON file.
     *
     * @return the list of stored customers, or an empty list if the file
     *         does not exist
     */
    public List<Customer> loadAllCustomers() {
        return readList(CUSTOMERS_FILE, CUSTOMERS_TYPE);
    }

    /**
     * Saves all sellers to the sellers JSON file, overwriting its previous
     * contents.
     *
     * @param sellers the list of sellers to persist
     */
    public void saveAllSellers(List<Seller> sellers) {
        writeList(SELLERS_FILE, sellers, SELLERS_TYPE);
    }

    /**
     * Loads all sellers from the sellers JSON file.
     *
     * @return the list of stored sellers, or an empty list if the file does
     *         not exist
     */
    public List<Seller> loadAllSellers() {
        return readList(SELLERS_FILE, SELLERS_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of people to persist
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
            System.err.println("Error saving people to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of people from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the concrete person type stored in the file
     * @return the stored people, or an empty list if the file does not
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
            System.err.println("Error loading people from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}