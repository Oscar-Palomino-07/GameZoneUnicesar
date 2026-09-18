package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the warranty module of GameZone Unicesar.
 * Warranties are stored in a CSV file under the {@code data} directory. If the
 * file does not exist yet, the repository loads an empty list.
 *
 * <p>Each row stores a discriminator ({@code BASIC} or {@code EXTENDED}) that
 * lets the repository rebuild the concrete warranty subtype when the file is
 * loaded. Because a warranty holds a full {@link Sale} and {@link Product},
 * the CSV only keeps their identifiers: when the file is loaded, the sale is
 * found through the {@link SaleRepository} and the product is found inside
 * that sale. The end date is not stored because every warranty calculates it
 * from its start date and duration.</p>
 */
public class WarrantyRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String WARRANTIES_FILE = "data/warranties.csv";
    private static final String CSV_HEADER = "type,id,productId,saleId,startDate";
    private static final String BASIC = "BASIC";
    private static final String EXTENDED = "EXTENDED";

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
     * Saves all warranties to the warranties CSV file, overwriting its
     * previous contents. Each warranty is written as one row: the type
     * discriminator, the warranty identifier, the product and sale
     * identifiers and the start date.
     *
     * @param warranties the list of warranties to persist
     */
    public void saveAll(List<Warranty> warranties) {
        try {
            Path path = Paths.get(WARRANTIES_FILE);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(CSV_HEADER);
                writer.newLine();
                for (Warranty warranty : warranties) {
                    String type = warranty instanceof ExtendedWarranty ? EXTENDED : BASIC;
                    writer.write(String.join(",", type, warranty.getId(), warranty.getProduct().getId(),
                            warranty.getSale().getId(), warranty.getStartDate().toString()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving warranties to " + WARRANTIES_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Loads all warranties from the warranties CSV file, rebuilding every row
     * as its concrete subtype through the type discriminator. Warranties whose
     * sale or product can no longer be found are skipped.
     *
     * @return the list of stored warranties, or an empty list if the file
     *         does not exist or cannot be read
     */
    public List<Warranty> loadAll() {
        List<Warranty> warranties = new ArrayList<>();
        Path path = Paths.get(WARRANTIES_FILE);
        if (!Files.exists(path)) {
            return warranties;
        }
        List<Sale> sales = saleRepository.loadAll();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            // Skips the header row.
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length < 5) {
                    System.err.println("Skipping warranty row with invalid format: " + line);
                    continue;
                }
                String type = fields[0];
                String id = fields[1];
                String productId = fields[2];
                String saleId = fields[3];
                Sale sale = findSale(sales, saleId);
                Product product = sale != null ? findProduct(sale, productId) : null;
                if (product == null) {
                    System.err.println("Skipping warranty " + id + ": sale or product not found.");
                    continue;
                }
                try {
                    LocalDate startDate = LocalDate.parse(fields[4]);
                    if (EXTENDED.equals(type)) {
                        warranties.add(new ExtendedWarranty(id, product, sale, startDate));
                    } else {
                        warranties.add(new BasicWarranty(id, product, sale, startDate));
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    System.err.println("Skipping warranty " + id + ": invalid start date " + fields[4] + ".");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading warranties from " + WARRANTIES_FILE + ": " + e.getMessage());
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