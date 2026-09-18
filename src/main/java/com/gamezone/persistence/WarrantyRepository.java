package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
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
import java.time.LocalDate;
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
     * Loads all warranties from the warranties JSON file. Warranties whose
     * sale or product can no longer be found are skipped.
     *
     * @return the list of stored warranties, or an empty list if the file
     *         does not exist or cannot be parsed
     */
    public List<Warranty> loadAll() {
        List<Warranty> warranties = new ArrayList<>();
        Path path = Paths.get(WARRANTIES_FILE);
        if (!Files.exists(path)) {
            return warranties;
        }
        List<WarrantyRecord> records;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            records = gson.fromJson(reader, RECORDS_TYPE);
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading warranties from " + WARRANTIES_FILE + ": " + e.getMessage());
            return warranties;
        }
        if (records == null) {
            return warranties;
        }

        List<Sale> sales = saleRepository.loadAll();
        for (WarrantyRecord record : records) {
            Sale sale = findSale(sales, record.saleId);
            Product product = sale != null ? findProduct(sale, record.productId) : null;
            if (product == null) {
                System.err.println("Skipping warranty " + record.id + ": sale or product not found.");
                continue;
            }
            LocalDate startDate = LocalDate.parse(record.startDate);
            if (EXTENDED.equals(record.type)) {
                warranties.add(new ExtendedWarranty(record.id, product, sale, startDate));
            } else {
                warranties.add(new BasicWarranty(record.id, product, sale, startDate));
            }
        }
        return warranties;
    }

    /**
     * Finds a sale by its identifier.
     *
     * @param sales  the stored sales
     * @param saleId the identifier to look for
     * @return the matching sale, or {@code null} if it does not exist
     */
    private Sale findSale(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Finds a product by its identifier among the products of a sale.
     *
     * @param sale      the sale to search in
     * @param productId the identifier to look for
     * @return the matching product, or {@code null} if the sale does not include it
     */
    private Product findProduct(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
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