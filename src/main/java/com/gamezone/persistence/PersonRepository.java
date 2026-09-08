package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the file-based persistence of the people managed by the store.
 *
 * <p>Customers and sellers are stored in two plain text files inside the
 * {@code data} directory. Every line represents a single person using a
 * pipe-delimited format that starts with a discriminator indicating its
 * type ({@code CUSTOMER} or {@code SELLER}).</p>
 *
 * <p>This class belongs to the persistence layer and does not contain any
 * business rule; its only responsibility is to save and recover people
 * from files.</p>
 */
public class PersonRepository {

    private static final String CUSTOMER_DISCRIMINATOR = "CUSTOMER";
    private static final String SELLER_DISCRIMINATOR = "SELLER";
    private static final String FIELD_SEPARATOR = "\\|";
    private static final Path DATA_DIRECTORY = Paths.get("data");
    private static final Path CUSTOMERS_FILE = DATA_DIRECTORY.resolve("customers.txt");
    private static final Path SELLERS_FILE = DATA_DIRECTORY.resolve("sellers.txt");

    /**
     * Saves the given customers to the customers file, overwriting its
     * previous content.
     *
     * @param customers the customers to persist
     */
    public void saveCustomers(List<Customer> customers) {
        writeFile(toPersonLines(customers), CUSTOMERS_FILE);
    }

    /**
     * Saves the given sellers to the sellers file, overwriting its
     * previous content.
     *
     * @param sellers the sellers to persist
     */
    public void saveSellers(List<Seller> sellers) {
        writeFile(toPersonLines(sellers), SELLERS_FILE);
    }

    /**
     * Loads all customers stored in the customers file.
     *
     * <p>If the file does not exist yet, an empty list is returned so that
     * the system can start with an empty customer registry.</p>
     *
     * @return the list of stored customers
     */
    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (Person person : loadFile(CUSTOMERS_FILE)) {
            if (person instanceof Customer customer) {
                customers.add(customer);
            }
        }
        return customers;
    }

    /**
     * Loads all sellers stored in the sellers file.
     *
     * <p>If the file does not exist yet, an empty list is returned.</p>
     *
     * @return the list of stored sellers
     */
    public List<Seller> loadSellers() {
        List<Seller> sellers = new ArrayList<>();
        for (Person person : loadFile(SELLERS_FILE)) {
            if (person instanceof Seller seller) {
                sellers.add(seller);
            }
        }
        return sellers;
    }

    /**
     * Serializes every person into its pipe-delimited line representation.
     *
     * @param people the people to serialize
     * @return the list of text lines
     */
    private List<String> toPersonLines(List<? extends Person> people) {
        List<String> lines = new ArrayList<>();
        for (Person person : people) {
            lines.add(toLine(person));
        }
        return lines;
    }

    /**
     * Builds the pipe-delimited line that represents a single person.
     *
     * @param person the person to serialize
     * @return the text line representation of the person
     */
    private String toLine(Person person) {
        List<String> fields = new ArrayList<>();
        if (person instanceof Customer customer) {
            fields.add(CUSTOMER_DISCRIMINATOR);
            fields.add(customer.getName());
            fields.add(customer.getIdentification());
            fields.add(customer.getPhone());
            fields.add(customer.getEmail());
        } else if (person instanceof Seller seller) {
            fields.add(SELLER_DISCRIMINATOR);
            fields.add(seller.getName());
            fields.add(seller.getIdentification());
            fields.add(seller.getPhone());
            fields.add(seller.getEmployeeCode());
            fields.add(seller.getShift());
        }
        return String.join("|", fields);
    }

    /**
     * Reads every non-empty line of the given file and parses it back into
     * a person object.
     *
     * @param file the file to read
     * @return the list of deserialized people
     */
    private List<Person> loadFile(Path file) {
        List<Person> people = new ArrayList<>();
        if (!Files.exists(file)) {
            return people;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Person person = parseLine(line.trim());
                if (person != null) {
                    people.add(person);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error loading people from " + file, exception);
        }
        return people;
    }

    /**
     * Parses a pipe-delimited line back into a person object.
     *
     * @param line the text line to parse
     * @return the person represented by the line, or {@code null} when the
     *         line is malformed or corresponds to an unknown type
     */
    private Person parseLine(String line) {
        if (line.isEmpty()) {
            return null;
        }
        String[] fields = line.split(FIELD_SEPARATOR);
        if (fields.length == 5 && CUSTOMER_DISCRIMINATOR.equals(fields[0])) {
            return new Customer(fields[1], fields[2], fields[3], fields[4]);
        }
        if (fields.length == 6 && SELLER_DISCRIMINATOR.equals(fields[0])) {
            return new Seller(fields[1], fields[2], fields[3], fields[4], fields[5]);
        }
        return null;
    }

    /**
     * Writes the given lines to the specified file, creating the parent
     * directory when required.
     *
     * @param lines the lines to persist
     * @param file  the target data file
     */
    private void writeFile(List<String> lines, Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error saving people to " + file, exception);
        }
    }
}