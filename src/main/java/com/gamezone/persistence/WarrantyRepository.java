package com.gamezone.persistence;

import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Warranty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
 * <p>A warranty holds a full {@link Sale} and {@link Product}, so the file only
 * keeps their identifiers through the {@link WarrantyRecord} class. When the
 * file is loaded, the sale is found through the {@link SaleRepository} and the
 * product is found inside that sale. The end date is not stored because every
 * warranty calculates it from its start date and duration.</p>
 */
public class WarrantyRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String WARRANTIES_FILE = "data/warranties.json";
    private static final String BASIC = "BASIC";
    private static final String EXTENDED = "EXTENDED";

    private static final Type RECORDS_TYPE = new TypeToken<List<WarrantyRecord>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final SaleRepository saleRepository;

    /**
     * Creates the warranty repository.
     *
     * @param saleRepository the repository used to find the sale, and the
     *                       product inside it, referenced by each warranty
     */
    public WarrantyRepository(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Saves all warranties to the warranties JSON file, overwriting its
     * previous contents.
     *
     * @param warranties the list of warranties to persist
     */
    public void saveAll(List<Warranty> warranties) {
        List<WarrantyRecord> records = new ArrayList<>();
        for (Warranty warranty : warranties) {
            String type = warranty instanceof ExtendedWarranty ? EXTENDED : BASIC;
            records.add(new WarrantyRecord(type, warranty.getId(), warranty.getProduct().getId(),
                    warranty.getSale().getId(), warranty.getStartDate().toString()));
        }
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
     * Simple structure written to the JSON file for each warranty. It keeps
     * only the warranty type, its identifier, the identifiers of the product
     * and the sale, and the start date.
     */
    private static class WarrantyRecord {

        private final String type;
        private final String id;
        private final String productId;
        private final String saleId;
        private final String startDate;

        WarrantyRecord(String type, String id, String productId, String saleId, String startDate) {
            this.type = type;
            this.id = id;
            this.productId = productId;
            this.saleId = saleId;
            this.startDate = startDate;
        }
    }
}