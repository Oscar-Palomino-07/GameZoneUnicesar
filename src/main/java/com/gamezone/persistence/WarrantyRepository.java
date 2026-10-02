package com.gamezone.persistence;

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
 * Handles file-based persistence for the warranty module of GameZone Unicesar.
 * Warranties are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 *
 * <p>This repository only moves identifiers in and out of the file: a stored
 * warranty is described by its type, its own identifier, the identifiers of
 * the covered product and of the sale, and its start date. It does not know
 * what a {@code Sale} or a {@code Product} is, and it does not resolve those
 * references; that work belongs to
 * {@link com.gamezone.service.WarrantyService}, which injects the sale and
 * product dependencies it needs. Keeping the resolution out of the
 * persistence layer is what allows the whole object graph to be built with
 * constructor injection, because the repositories no longer depend on any
 * service and therefore cannot form a cycle with them.</p>
 */
public class WarrantyRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String WARRANTIES_FILE = "data/warranties.json";
    private static final String BASIC = "BASIC";
    private static final String EXTENDED = "EXTENDED";

    private static final Type RECORDS_TYPE = new TypeToken<List<WarrantyRecord>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates the warranty repository. It has no dependencies on purpose: the
     * only thing it needs to do is read and write {@link WarrantyRecord}
     * entries.
     */
    public WarrantyRepository() {
    }

    /**
     * Saves all the warranty records to the warranties JSON file, overwriting
     * its previous contents.
     *
     * @param records the records to persist
     */
    public void saveAll(List<WarrantyRecord> records) {
        try {
            Path path = Paths.get(WARRANTIES_FILE);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(records, RECORDS_TYPE, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving warranties to " + WARRANTIES_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Loads all the warranty records from the warranties JSON file. The records
     * are returned exactly as they are stored, without resolving the sale or
     * the product they reference.
     *
     * @return the stored records, or an empty list if the file does not exist
     *         or cannot be parsed
     */
    public List<WarrantyRecord> loadAll() {
        Path path = Paths.get(WARRANTIES_FILE);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<WarrantyRecord> records = gson.fromJson(reader, RECORDS_TYPE);
            return records != null ? records : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading warranties from " + WARRANTIES_FILE + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tells whether the given discriminator represents an extended warranty.
     *
     * @param type the stored warranty type
     * @return {@code true} when the record describes an extended warranty
     */
    public static boolean isExtended(String type) {
        return EXTENDED.equals(type);
    }

    /**
     * The value stored in the file for the basic warranty type.
     *
     * @return the basic warranty discriminator
     */
    public static String basicType() {
        return BASIC;
    }

    /**
     * The value stored in the file for the extended warranty type.
     *
     * @return the extended warranty discriminator
     */
    public static String extendedType() {
        return EXTENDED;
    }

    /**
     * Plain data structure written to the JSON file for each warranty. It
     * keeps only the warranty type, the warranty identifier, the identifiers
     * of the product and of the sale, and the start date. The end date is not
     * stored because every warranty derives it from its start date and its
     * duration.
     */
    public static final class WarrantyRecord {

        private final String type;
        private final String id;
        private final String productId;
        private final String saleId;
        private final String startDate;

        /**
         * Creates a warranty record with its stored fields.
         *
         * @param type      the warranty type discriminator, BASIC or EXTENDED
         * @param id        the warranty identifier
         * @param productId the identifier of the covered product
         * @param saleId    the identifier of the sale that covers the product
         * @param startDate the start date of the coverage
         */
        public WarrantyRecord(String type, String id, String productId, String saleId, String startDate) {
            this.type = type;
            this.id = id;
            this.productId = productId;
            this.saleId = saleId;
            this.startDate = startDate;
        }

        /**
         * @return the warranty type discriminator
         */
        public String getType() {
            return type;
        }

        /**
         * @return the warranty identifier
         */
        public String getId() {
            return id;
        }

        /**
         * @return the identifier of the covered product
         */
        public String getProductId() {
            return productId;
        }

        /**
         * @return the identifier of the sale that covers the product
         */
        public String getSaleId() {
            return saleId;
        }

        /**
         * @return the start date of the coverage, as text
         */
        public String getStartDate() {
            return startDate;
        }
    }
}